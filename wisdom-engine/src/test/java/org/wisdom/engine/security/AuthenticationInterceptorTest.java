package org.wisdom.engine.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.security.Authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Check the behavior of the Authentication Interceptor
 */
public class AuthenticationInterceptorTest {

    private String username;
    private Answer usernameAnswer = new Answer() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            AuthenticationInterceptorTest.this.username = (String) invocation.getArguments()[0];
            return null;
        }
    };

    @Before
    public void setUp() {
        username = null;
    }

    @Test
    public void testSuccessfulAuthWithOnlyOneAuthenticator() throws Throwable {
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor();
        interceptor.authenticators = new Authenticator[] {new TrueAuthenticator()};

        Authenticated authenticated = mock(Authenticated.class);
        RequestContext ic = mock(RequestContext.class);
        Context ctx = mock(Context.class);
        Request request = mock(Request.class);
        when(ic.context()).thenReturn(ctx);
        when(ic.proceed()).thenReturn(Results.ok("authenticated"));
        when(ctx.request()).thenReturn(request);
        doAnswer(usernameAnswer).when(request).setUsername(anyString());

        assertThat(interceptor.call(authenticated, ic).getStatusCode()).isEqualTo(200);
        assertThat(username).isEqualTo("admin");
    }

    @Test
    public void testFailedAuthWithOnlyOneAuthenticator() throws Throwable {
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor();
        interceptor.authenticators = new Authenticator[] {new FalseAuthenticator()};

        Authenticated authenticated = mock(Authenticated.class);
        RequestContext ic = mock(RequestContext.class);
        Context ctx = mock(Context.class);
        Request request = mock(Request.class);
        when(ic.context()).thenReturn(ctx);
        when(ic.proceed()).thenReturn(Results.ok("authenticated"));
        when(ctx.request()).thenReturn(request);
        doAnswer(usernameAnswer).when(request).setUsername(anyString());

        assertThat(interceptor.call(authenticated, ic).getStatusCode()).isEqualTo(401);
        assertThat(username).isNull();
    }

    @Test
    public void testAuthWithNoAuthenticator() throws Throwable {
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor();
        interceptor.authenticators = new Authenticator[] {};

        Authenticated authenticated = mock(Authenticated.class);
        RequestContext ic = mock(RequestContext.class);
        Context ctx = mock(Context.class);
        Request request = mock(Request.class);
        when(ic.context()).thenReturn(ctx);
        when(ic.proceed()).thenReturn(Results.ok("authenticated"));
        when(ctx.request()).thenReturn(request);
        doAnswer(usernameAnswer).when(request).setUsername(anyString());

        assertThat(interceptor.call(authenticated, ic).getStatusCode()).isEqualTo(401);
        assertThat(username).isNull();
    }

    @Test
    public void testAuthWithNoMatchingAuthenticator() throws Throwable {
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor();
        interceptor.authenticators = new Authenticator[] {new TrueAuthenticator()};

        Authenticated authenticated = mock(Authenticated.class);
        Class<? extends Authenticator> aa = AdminAuthenticator.class;
        when(authenticated.value()).thenReturn((Class) aa);
        RequestContext ic = mock(RequestContext.class);
        Context ctx = mock(Context.class);
        Request request = mock(Request.class);
        when(ic.context()).thenReturn(ctx);
        when(ic.proceed()).thenReturn(Results.ok("authenticated"));
        when(ctx.request()).thenReturn(request);
        doAnswer(usernameAnswer).when(request).setUsername(anyString());

        assertThat(interceptor.call(authenticated, ic).getStatusCode()).isEqualTo(401);
        assertThat(username).isNull();
    }

    @Test
    public void testAuthWithMatchingAuthenticator() throws Throwable {
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor();
        interceptor.authenticators = new Authenticator[] {new TrueAuthenticator(), new AdminAuthenticator()};

        Authenticated authenticated = mock(Authenticated.class);
        Class<? extends Authenticator> aa = AdminAuthenticator.class;
        when(authenticated.value()).thenReturn((Class) aa);
        RequestContext ic = mock(RequestContext.class);
        Context ctx = mock(Context.class);
        Request request = mock(Request.class);
        when(ctx.parameter("username")).thenReturn("admin");
        when(ic.context()).thenReturn(ctx);
        when(ic.proceed()).thenReturn(Results.ok("authenticated"));
        when(ctx.request()).thenReturn(request);
        doAnswer(usernameAnswer).when(request).setUsername(anyString());

        assertThat(interceptor.call(authenticated, ic).getStatusCode()).isEqualTo(200);
        assertThat(username).isEqualTo("admin");
    }


    private class TrueAuthenticator implements Authenticator {

        @Override
        public String getUserName(Context context) {
            return "admin";
        }

        @Override
        public Result onUnauthorized(Context context) {
            return Results.unauthorized();
        }
    }

    private class FalseAuthenticator implements Authenticator {

        @Override
        public String getUserName(Context context) {
            return null;
        }

        @Override
        public Result onUnauthorized(Context context) {
            return Results.unauthorized();
        }
    }

    private class AdminAuthenticator implements Authenticator {

        @Override
        public String getUserName(Context context) {
            String name = context.parameter("username");
            if(name != null  && "admin".equals(name)) {
                return "admin";
            } else {
                return null;
            }
        }

        @Override
        public Result onUnauthorized(Context context) {
            return Results.unauthorized();
        }
    }
}
