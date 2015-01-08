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

import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.crypto.Hash;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.crypto.CryptoServiceSingleton;
import org.wisdom.framework.csrf.CSRFServiceImpl;
import org.wisdom.framework.csrf.DefaultCSRFErrorHandler;
import org.wisdom.framework.csrf.api.CSRFErrorHandler;
import org.wisdom.framework.csrf.api.CSRFService;
import org.wisdom.test.parents.FakeContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSRFServiceImplTest {

    public static final String SECRET = "JYFVq6:^jrh:KIy:yM5Xb<sH58WW80OLL4_gCL4Ne[PnAJ9QC/Z?LG2dbwoSkiBL";
    public static final String CSRF_TOKEN = "csrf_token";
    public static final String CSRF_COOKIE = "csrf_cookie";

    CSRFServiceImpl service = new CSRFServiceImpl();

    @Before
    public void prepare() {
        service.crypto = new CryptoServiceSingleton(SECRET, Hash.MD5, 128, Crypto.AES_CBC_ALGORITHM, 20);
    }

    @Test
    public void testRegularTokenGeneration() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(false);
        FakeContext context = new FakeContext();

        String token = service.generateToken(context);
        assertThat(token).isNotNull();
        assertThat(context.request().data().get(CSRFService.TOKEN_KEY)).isNotNull().isEqualTo(token);

        String token2 = service.generateToken(context);
        assertThat(token2).isNotNull().isNotEqualTo(token);
        assertThat(context.request().data().get(CSRFService.TOKEN_KEY)).isNotNull().isEqualTo(token2);
    }

    @Test
    public void testSignedRegularTokenGeneration() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        FakeContext context = new FakeContext();

        String token = service.generateToken(context);
        assertThat(token).isNotNull();
        assertThat(context.request().data().get(CSRFService.TOKEN_KEY)).isNotNull().isEqualTo(token);

        String token2 = service.generateToken(context);
        assertThat(token2).isNotNull().isNotEqualTo(token);
        assertThat(context.request().data().get(CSRFService.TOKEN_KEY)).isNotNull().isEqualTo(token2);
    }

    @Test
    public void testThatWeAcceptRequestContainingTheTokenInTheQuery() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context1 = new FakeContext();
        context1.addToSession(CSRF_TOKEN, token).setParameter(CSRF_TOKEN, token)
                .route(new Route(HttpMethod.GET, "/", null, null));
        assertThat(service.isValidRequest(context1)).isTrue();

        FakeContext context2 = new FakeContext();
        context2.addToSession(CSRF_TOKEN, token).setParameter(CSRF_TOKEN, token)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context2)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheTokenInFormBody() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.addToSession(CSRF_TOKEN, token).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();

        context = new FakeContext();
        context.addToSession(CSRF_TOKEN, token).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.MULTIPART)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheTokenInHeader() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.addToSession(CSRF_TOKEN, token).setHeader(CSRFServiceImpl.CSRF_TOKEN_HEADER, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheNoCheckHeader() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.setHeader(CSRFServiceImpl.CSRF_TOKEN_HEADER, CSRFServiceImpl.NO_CHECK_HEADER_VALUE)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();


        context = new FakeContext();
        context
                .addToSession(CSRF_TOKEN, token)
                .setHeader(CSRFServiceImpl.CSRF_TOKEN_HEADER, CSRFServiceImpl.NO_CHECK_HEADER_VALUE)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheAjaxHeader() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context
                .addToSession(CSRF_TOKEN, token)
                .setHeader(CSRFServiceImpl.AJAX_HEADER, "I'm a teapot")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();

        context = new FakeContext();
        context
                .setHeader(CSRFServiceImpl.AJAX_HEADER, "I'm a teapot")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeRejectRequestContainingABadTokenInFormBody() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.addToSession(CSRF_TOKEN, token).setFormField(CSRF_TOKEN, "I'm a teapot")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingABadSignedTokenInFormBody() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);

        String token = service.crypto.generateSignedToken();

        FakeContext context = new FakeContext();
        context.addToSession(CSRF_TOKEN, token).setFormField(CSRF_TOKEN, "I-M-T")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingTokenInSessionButNoneElsewhere() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context
                .addToSession(CSRF_TOKEN, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingTokenInTheBodyButNotInTheSession() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context
                .setFormField(CSRF_TOKEN, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testAddTokenToResult() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);

        FakeContext context = new FakeContext();
        Result result = service.addTokenToResult(context, "token", Results.ok());

        assertThat(context.session().get(CSRF_TOKEN)).isEqualToIgnoringCase("token");
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
    }

    @Test
    public void testAddTokenToResultUsingCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);


        FakeContext context = new FakeContext();
        Result result = service.addTokenToResult(context, "token", Results.ok());

        assertThat(context.session().get(CSRF_TOKEN)).isNull();
        assertThat(result.getCookie(CSRF_COOKIE).value()).isEqualTo("token");
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
    }

    @Test
    public void testAddTokenToResultUsingExistingCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);

        FakeContext context = new FakeContext().setCookie(Cookie.cookie(CSRF_COOKIE, "another token").build());
        Result result = service.addTokenToResult(context, "token", Results.ok());

        assertThat(context.session().get(CSRF_TOKEN)).isNull();
        assertThat(result.getCookie(CSRF_COOKIE).value()).isEqualTo("token");
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
    }


    @Test
    public void testThatWeRejectRequestContainingUnsignedTokenInTheBody() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);

        // Add a signed token in session and a raw token in the body.
        String raw = service.crypto.generateToken();

        FakeContext context = new FakeContext().addToSession(CSRF_TOKEN, service.crypto.generateSignedToken());
        context
                .setFormField(CSRF_TOKEN, raw)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingUnsignedTokenInTheSession() {
        service.configuration = mock(Configuration.class);
        service.handler = new DefaultCSRFErrorHandler();
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);

        // Add a signed token in the body but not in the session
        String raw = service.crypto.generateToken();

        FakeContext context = new FakeContext().addToSession(CSRF_TOKEN, raw);
        context
                .setFormField(CSRF_TOKEN, service.crypto.generateSignedToken())
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
        Result result = service.clearTokenIfInvalid(context, "Bad token");
        assertThat(context.session().get(CSRF_TOKEN)).isNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.FORBIDDEN);
    }

    @Test
    public void testThatWeGenerateANewTokenInEachRequest() throws InterruptedException {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);

        String token = service.crypto.generateSignedToken();
        FakeContext context = new FakeContext().addToSession(CSRF_TOKEN, token).setFormField(CSRF_TOKEN, token);
        context.route(new Route(HttpMethod.POST, "/", null, null));
        Thread.sleep(2);
        String resigned = service.extractTokenFromRequest(context);
        assertThat(token).isNotEqualTo(resigned);
        assertThat(service.crypto.compareSignedTokens(token, resigned)).isTrue();
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheTokenInTheQueryAndInACookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(false);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);
        String token = service.crypto.generateToken();

        FakeContext context1 = new FakeContext();
        context1.setCookie(createCSRFCookie(token)).setParameter(CSRF_TOKEN, token)
                .route(new Route(HttpMethod.GET, "/", null, null));
        assertThat(service.isValidRequest(context1)).isTrue();

        FakeContext context2 = new FakeContext();
        context2.setCookie(createCSRFCookie(token)).setParameter(CSRF_TOKEN, token)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context2)).isTrue();

        assertThat(context2.session().get(CSRF_TOKEN)).isNull();
    }

    @Test
    public void testThatWeAcceptRequestContainingASignedTokenInTheQueryAndInACookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);
        String token = service.crypto.generateSignedToken();

        FakeContext context1 = new FakeContext();
        context1.setCookie(createCSRFCookie(token)).setParameter(CSRF_TOKEN, token)
                .route(new Route(HttpMethod.GET, "/", null, null));
        assertThat(service.isValidRequest(context1)).isTrue();

        FakeContext context2 = new FakeContext();
        context2.setCookie(createCSRFCookie(token)).setParameter(CSRF_TOKEN, token)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context2)).isTrue();

        assertThat(context2.session().get(CSRF_TOKEN)).isNull();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheSignedTokenInFormBodyAndInACookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateSignedToken();

        FakeContext context = new FakeContext();
        context.setCookie(createCSRFCookie(token)).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();

        context = new FakeContext();
        context.setCookie(createCSRFCookie(token)).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.MULTIPART)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheTokenInFormBodyAndInACookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(false);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.setCookie(createCSRFCookie(token)).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();

        context = new FakeContext();
        context.setCookie(createCSRFCookie(token)).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.MULTIPART)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheSignedTokenInHeaderUsingCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateSignedToken();

        FakeContext context = new FakeContext();
        context.setCookie(createCSRFCookie(token)).setHeader(CSRFServiceImpl.CSRF_TOKEN_HEADER, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheTokenInHeaderUsingCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(false);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.setCookie(createCSRFCookie(token)).setHeader(CSRFServiceImpl.CSRF_TOKEN_HEADER, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeRejectRequestContainingSignedTokenInACookieButNoneElsewhere() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        String token = service.crypto.generateSignedToken();

        FakeContext context = new FakeContext();
        context
                .setCookie(createCSRFCookie(token))
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingTokenInACookieButNoneElsewhere() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(false);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context
                .setCookie(createCSRFCookie(token))
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingSignedTokenInTheBodyButNotInTheCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);
        String token = service.crypto.generateSignedToken();
        // 1) No cookie at all
        FakeContext context = new FakeContext();
        context
                .setFormField(CSRF_TOKEN, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();

        // 2) Empty cookie
        context = new FakeContext();
        context
                .setCookie(createCSRFCookie(""))
                .setFormField(CSRF_TOKEN, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingTokenInTheBodyButNotInTheCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(false);
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);
        String token = service.crypto.generateToken();
        // 1) No cookie at all
        FakeContext context = new FakeContext();
        context
                .setFormField(CSRF_TOKEN, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();

        // 2) Empty cookie
        context = new FakeContext();
        context
                .setCookie(createCSRFCookie(""))
                .setFormField(CSRF_TOKEN, token)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheNoCheckHeaderUsingCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.setHeader(CSRFServiceImpl.CSRF_TOKEN_HEADER, CSRFServiceImpl.NO_CHECK_HEADER_VALUE)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();


        context = new FakeContext();
        context
                .addToSession(CSRF_TOKEN, token)
                .setHeader(CSRFServiceImpl.CSRF_TOKEN_HEADER, CSRFServiceImpl.NO_CHECK_HEADER_VALUE)
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheAjaxHeaderUsingCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context
                .setCookie(createCSRFCookie(token))
                .setHeader(CSRFServiceImpl.AJAX_HEADER, "I'm a teapot")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();

        context = new FakeContext();
        context
                .setHeader(CSRFServiceImpl.AJAX_HEADER, "I'm a teapot")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatWeRejectRequestContainingABadTokenInFormBodyUsingACookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateToken();

        FakeContext context = new FakeContext();
        context.setCookie(createCSRFCookie(token))
                .setFormField(CSRF_TOKEN, "I'm a teapot")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeRejectRequestContainingABadSignedTokenInFormBodyUsingACookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        String token = service.crypto.generateSignedToken();

        FakeContext context = new FakeContext();
        context.setCookie(createCSRFCookie(token))
                .setFormField(CSRF_TOKEN, "I'm a teapot")
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
    }

    @Test
    public void testThatWeAcceptRequestContainingTheTokenInFormBodyAndInASignedCookie() {
        service.configuration = mock(Configuration.class);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);
        when(service.configuration.getBooleanWithDefault("cookie.secure", true)).thenReturn(true);
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        String token = service.crypto.generateSignedToken();

        FakeContext context = new FakeContext();
        context.setCookie(createSecuredCSRFCookie(token)).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();

        context = new FakeContext();
        context.setCookie(createSecuredCSRFCookie(token)).setFormField(CSRF_TOKEN, token).setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.MULTIPART)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isTrue();
    }

    @Test
    public void testThatTheHandlerCanBeReplaced() {
        service.configuration = mock(Configuration.class);
        service.handler = new CSRFErrorHandler() {
            @Override
            public Result onError(Context context, String reason) {
                return Results.unauthorized(reason);
            }
        };
        when(service.configuration.getWithDefault("token.name", "csrfToken")).thenReturn(CSRF_TOKEN);
        when(service.configuration.getBooleanWithDefault("token.sign", true)).thenReturn(true);

        // Add a signed token in the body but not in the session
        String raw = service.crypto.generateToken();

        FakeContext context = new FakeContext().addToSession(CSRF_TOKEN, raw);
        context
                .setFormField(CSRF_TOKEN, service.crypto.generateSignedToken())
                .setFormField("key", "value")
                .setHeader(HeaderNames.CONTENT_TYPE, MimeTypes.FORM)
                .route(new Route(HttpMethod.POST, "/", null, null));
        assertThat(service.isValidRequest(context)).isFalse();
        Result result = service.clearTokenIfInvalid(context, "Bad token");
        assertThat(context.session().get(CSRF_TOKEN)).isNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.UNAUTHORIZED);
    }


    private Cookie createCSRFCookie(String token) {
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);
        return Cookie.cookie(CSRF_COOKIE, token).build();
    }

    private Cookie createSecuredCSRFCookie(String token) {
        when(service.configuration.get("cookie.name")).thenReturn(CSRF_COOKIE);
        return Cookie.cookie(CSRF_COOKIE, token).setSecure(true).build();
    }

}