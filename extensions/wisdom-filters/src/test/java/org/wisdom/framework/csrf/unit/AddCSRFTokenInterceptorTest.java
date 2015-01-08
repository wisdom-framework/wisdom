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
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.crypto.Hash;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.crypto.CryptoServiceSingleton;
import org.wisdom.framework.csrf.AddCSRFTokenInterceptor;
import org.wisdom.framework.csrf.CSRFServiceImpl;
import org.wisdom.framework.csrf.api.AddCSRFToken;
import org.wisdom.test.parents.FakeContext;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddCSRFTokenInterceptorTest {


    AddCSRFToken add = mock(AddCSRFToken.class);
    AddCSRFTokenInterceptor interceptor = new AddCSRFTokenInterceptor();
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
        interceptor.csrf = service;
    }

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
    }

    @Test
    public void testAnnotation() throws Exception {
        assertThat(interceptor.annotation()).isEqualTo(AddCSRFToken.class);
    }

    @Test
    public void testThatWeAddTokenWhenNotThere() throws Exception {
        FakeContext context = new FakeContext();
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(
                CSRFServiceImplTest.CSRF_TOKEN);
        route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("doSomething", String
                .class));
        context
                .setParameter("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(add, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNotNull().isNotEmpty();
    }

    @Test
    public void testThatWeDoNotAddTokenWhenThereIsOne() throws Exception {
        FakeContext context = new FakeContext();
        String token = service.crypto.generateToken();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .addToSession(CSRFServiceImplTest.CSRF_TOKEN, token)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(add, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isEqualTo(token);
    }

    @Test
    public void testThatWeDoNotAddATokenOnANonEligibleRequest() throws Exception {
        FakeContext context = new FakeContext();
        route = new Route(HttpMethod.POST, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .setHeader(HeaderNames.ACCEPT, MimeTypes.JSON)
                .route(route);

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(add, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNull();
    }

    @Test
    public void testThatTokenAreDifferentForEachRequest() throws Exception {
        when(add.newTokenOnEachRequest()).thenReturn(true);
        FakeContext context = new FakeContext();
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        final String token = service.crypto.generateSignedToken();
        route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setParameter("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .addToSession(CSRFServiceImplTest.CSRF_TOKEN, token)
                .setHeader(HeaderNames.CACHE_CONTROL, HeaderNames.NOCACHE_VALUE)
                .setHeader(HeaderNames.ACCEPT, MimeTypes.HTML)
                .route(route);

        assertThat(service.eligibleForCSRF(context)).isTrue();

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(add, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNotEqualTo(token);
    }

    @Test
    public void testThatTokensAreNotAddedToRequestThatSetCacheHeaders() throws Exception {
        when(add.newTokenOnEachRequest()).thenReturn(true);
        FakeContext context = new FakeContext();
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        final String token = service.crypto.generateSignedToken();
        route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setParameter("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .setHeader(HeaderNames.CACHE_CONTROL, "public, max-age=3600")
                .setHeader(HeaderNames.ACCEPT, MimeTypes.HTML)
                .route(route);

        assertThat(service.eligibleForCSRF(context)).isTrue();

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(add, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNull();
    }

    @Test
    public void testThatTokensAreAddedToRequestThatSetCacheHeadersToNoCache() throws Exception {
        when(add.newTokenOnEachRequest()).thenReturn(true);
        FakeContext context = new FakeContext();
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context
                .setParameter("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .setHeader(HeaderNames.CACHE_CONTROL, HeaderNames.NOCACHE_VALUE)
                .setHeader(HeaderNames.ACCEPT, MimeTypes.HTML)
                .route(route);

        assertThat(service.eligibleForCSRF(context)).isTrue();

        Context.CONTEXT.set(context);

        RequestContext rc = new RequestContext(route,
                Collections.<Filter>emptyList(), Collections.<Interceptor<?>, Object>emptyMap(),
                new Object[]{"value"}, null);

        Result result = interceptor.call(add, rc);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(context.session().get(CSRFServiceImplTest.CSRF_TOKEN)).isNotEmpty();
    }

    @Test
    public void testThatHeadRequestAcceptingHTMLAreEligible() throws Exception {
        FakeContext context = new FakeContext();
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        route = new Route(HttpMethod.HEAD, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context.setHeader(HeaderNames.ACCEPT, MimeTypes.HTML).route(route);
        assertThat(service.eligibleForCSRF(context)).isTrue();
    }


    @Test
    public void testThatHeadRequestNotAcceptingHTMLAreNotEligible() throws Exception {
        FakeContext context = new FakeContext();
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken"))
                .thenReturn(CSRFServiceImplTest.CSRF_TOKEN);
        route = new Route(HttpMethod.HEAD, "/", controller, controller.getClass().getMethod("doSomething", String.class));
        context.setHeader(HeaderNames.ACCEPT, MimeTypes.JSON).route(route);
        assertThat(service.eligibleForCSRF(context)).isFalse();
    }

}