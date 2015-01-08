/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.framework.csrf.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.FormParameter;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.crypto.Hash;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.crypto.CryptoServiceSingleton;
import org.wisdom.framework.csrf.CSRFInterceptor;
import org.wisdom.framework.csrf.CSRFServiceImpl;
import org.wisdom.framework.csrf.DefaultCSRFErrorHandler;
import org.wisdom.framework.csrf.api.CSRF;
import org.wisdom.test.parents.FakeContext;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSRFInterceptorTest {


    CSRF annotation = mock(CSRF.class);
    CSRFInterceptor interceptor = new CSRFInterceptor();
    CSRFServiceImpl service;
    private Route route;

    private Controller controller = new DefaultController() {

        public Result doSomething(@FormParameter("key") String value) {
            return ok(value);
        }

    };

    @Before
    public void setUp() {
        service = new CSRFServiceImpl();
        service.crypto = new CryptoServiceSingleton(CSRFServiceImplTest.SECRET,
                Hash.MD5, 128, Crypto.AES_CBC_ALGORITHM, 20);
        service.configuration = mock(Configuration.class);
        service.handler = new DefaultCSRFErrorHandler();
        interceptor.csrf = service;
    }

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
    }

    @Test
    public void testAnnotation() throws Exception {
        assertThat(interceptor.annotation()).isEqualTo(CSRF.class);
    }

    @Test
    public void testThatWeAcceptRequestWithATokenInTheQuery() throws Exception {
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .addToSession(CSRFServiceImplTest.CSRF_TOKEN, "token")
                .setParameter(CSRFServiceImplTest.CSRF_TOKEN, "token")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(annotation, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNotNull().isNotEmpty()
                .isNotEqualTo("token");

    }

    @Test
    public void testThatWeAcceptRequestWithATokenInTheForm() throws Exception {
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .setFormField(CSRFServiceImplTest.CSRF_TOKEN, "token")
                .addToSession(CSRFServiceImplTest.CSRF_TOKEN, "token")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(annotation, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNotNull().isNotEmpty()
                .isNotEqualTo("token");
    }

    @Test
    public void testThatWeAcceptRequestWithATokenInTheFormWhenUsingCookie() throws Exception {
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        when(service.configuration.get("cookie.name")).thenReturn(CSRFServiceImplTest.CSRF_COOKIE);
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .setFormField(CSRFServiceImplTest.CSRF_TOKEN, "token")
                .setCookie(Cookie.cookie(CSRFServiceImplTest.CSRF_COOKIE, "token").build())
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(annotation, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getCookie(CSRFServiceImplTest.CSRF_COOKIE).value()).isNotNull().isNotEmpty()
                .isNotEqualTo("token");
    }

    @Test
    public void testThatWeRejectRequestWithAnInvalidTokenInBody() throws Exception {
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .setFormField(CSRFServiceImplTest.CSRF_TOKEN, "another token")
                .addToSession(CSRFServiceImplTest.CSRF_TOKEN, "token")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(annotation, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.FORBIDDEN);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNull();
    }

    @Test
    public void testThatWeRejectRequestWithTokenInBodyButNotInSession() throws Exception {
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .setFormField(CSRFServiceImplTest.CSRF_TOKEN, "token")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(annotation, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.FORBIDDEN);
        // Cleared.
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNull();
    }

    @Test
    public void testThatWeRejectRequestWithInvalidTokenInBodyUsingCookie() throws Exception {
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        when(service.configuration.get("cookie.name")).thenReturn(CSRFServiceImplTest.CSRF_COOKIE);
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .setFormField(CSRFServiceImplTest.CSRF_TOKEN, "another token")
                .setCookie(Cookie.cookie(CSRFServiceImplTest.CSRF_COOKIE, "token").build())
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(annotation, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.FORBIDDEN);
        // Discarded cookie
        assertThat(result.getCookie(CSRFServiceImplTest.CSRF_COOKIE).value()).isEmpty();
        assertThat(result.getCookie(CSRFServiceImplTest.CSRF_COOKIE).maxAge()).isEqualTo(0);
    }

    @Test
    public void testThatATokenIsAddedOnEligibleRequests() throws Exception {
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        when(service.configuration.get("cookie.name")).thenReturn(CSRFServiceImplTest.CSRF_COOKIE);
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setParameter("key", "value")
                .setHeader(HeaderNames.ACCEPT, MimeTypes.HTML)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(annotation, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        // Cookie created.
        assertThat(result.getCookie(CSRFServiceImplTest.CSRF_COOKIE).value()).isNotEmpty();
        assertThat(result.getCookie(CSRFServiceImplTest.CSRF_COOKIE).maxAge()).isGreaterThan(100);
    }


}