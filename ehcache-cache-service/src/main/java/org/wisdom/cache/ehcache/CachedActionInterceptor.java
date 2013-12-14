package org.wisdom.cache.ehcache;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.LoggerFactory;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.cache.Cached;
import org.wisdom.api.http.Result;
import org.wisdom.api.interceptor.InterceptionContext;
import org.wisdom.api.interceptor.Interceptor;

/**
 * An action interceptor caching the result of an action and returning the cached result if it was cached already.
 */
@Component
@Provides(specifications = Interceptor.class)
@Instantiate
public class CachedActionInterceptor extends Interceptor<Cached> {

    @Requires
    private Cache cache;

    @Override
    public Result call(Cached configuration, InterceptionContext context) throws Throwable {
        Result result = (Result) cache.get(configuration.key());

        if (result == null) {
            result = context.proceed();
            cache.set(configuration.key(), result, configuration.duration());
            LoggerFactory.getLogger(this.getClass()).info("Caching result of " + context.request().uri() + " for " +
                    configuration.duration() + " seconds");
        }

        return result;
    }

    @Override
    public Class<Cached> annotation() {
        return Cached.class;
    }
}
