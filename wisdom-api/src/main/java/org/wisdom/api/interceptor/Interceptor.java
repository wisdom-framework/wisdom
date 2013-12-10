package org.wisdom.api.interceptor;


import org.wisdom.api.http.Result;

/**
 * Class extended by interceptors.
 * An interceptor <em>intercepts</em> request and can customize the input and output of the request. When several
 * interceptors are used on a single invocation, a chain is built. Interceptor must call the
 * {@link InterceptionContext#proceed()} method to call the next interceptor.
 * @param <A> the type of the annotation used to configure the interceptor.
 */
public abstract class Interceptor<A> {

    /**
     * The interception method. The method should call {@link InterceptionContext#proceed()} to call the next
     * interception. Without this call it cut the chain.
     * @param configuration the interception configuration
     * @param context the interception context
     * @return the result
     * @throws Throwable if anything bad happen
     */
    public abstract Result call(A configuration, InterceptionContext context) throws Throwable;

    /**
     * Gets the annotation class configuring the current interceptor
     * @return the annotation
     */
    public abstract Class<A> annotation();

    /**
     * Internal method.
     */
    public final Result call(InterceptionContext context, Object configuration) throws Throwable {
        return call((A) configuration, context);
    }

}
