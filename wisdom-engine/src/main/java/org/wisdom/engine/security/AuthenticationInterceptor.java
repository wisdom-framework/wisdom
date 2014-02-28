package org.wisdom.engine.security;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interceptor.InterceptionContext;
import org.wisdom.api.interceptor.Interceptor;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.security.Authenticator;

/**
 *
 */
@Component(immediate = true)
@Provides(specifications = Interceptor.class)
@Instantiate
public class AuthenticationInterceptor extends Interceptor<Authenticated> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Requires(optional = true)
    private Authenticator[] authenticators;


    /**
     * Intercepts the action, and checks if the current request is authenticated.
     * the results depends on two factors: if the request is authenticated and the availability of authenticator.
     *
     * If there are no authenticator, it returns a unauthorized response.
     * If there are an authenticator matching the set ones (in the annotation), use it. If the authenticator cannot
     * authenticate the request, it will be used to get the unauthorized response.
     * If these are no authenticator matching the request, it returns an unauthorized response.
     *
     * If the annotation does not specify the authenticator, it uses the first one. If several ones are available,
     * a warning is thrown.
     *
     * @param configuration the authenticated annotation instance
     * @param context       the interception context
     * @return the result
     * @throws Throwable if anything bad happen
     */
    @Override
    public Result call(Authenticated configuration, InterceptionContext context) throws Throwable {
        Authenticator authenticator = getAuthenticator(context, configuration.value());
        if (authenticator != null) {
            String username = authenticator.getUserName(context.context());
            if (username == null) {
                // We cut the interception chain on purpose.
                context.context().request().setUsername(null);
                return authenticator.onUnauthorized(context.context());
            } else {
                // Set the username.
                context.context().request().setUsername(username);
                return context.proceed();
            }
        } else {
            context.context().request().setUsername(null);
            // No authenticator
            return Results.unauthorized();
        }
    }

    private Authenticator getAuthenticator(InterceptionContext context, Class<? extends Authenticator> value) {
        if (authenticators.length == 0) {
            return null;
        }

        if (value.equals(Authenticator.class)) {
            // This is the default value.
            if (authenticators.length > 1) {
                // Default value but several authenticator
                LOGGER.warn("The action {} require authentication, but does not specify the authenticator. " +
                        "But, several authenticators are available, picked one randomly ({})",
                        context.context().path(), authenticators[0]);
            }
            return authenticators[0];
        }

        // Iterate over the authenticator to find the right one.
        for (Authenticator authenticator : authenticators) {
            if (authenticator.getClass().equals(value)) {
                return authenticator;
            }
        }

        return null;
    }

    /**
     * Gets the {@link org.wisdom.api.security.Authenticated} annotation class
     * @return the annotation
     */
    @Override
    public Class<Authenticated> annotation() {
        return Authenticated.class;
    }
}
