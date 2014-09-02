/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
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
package org.wisdom.cache.ehcache;

import com.google.common.base.Strings;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.cache.Cached;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;

/**
 * An action interceptor caching the result of an action and returning the cached result if it was cached already.
 */
@Component
@Provides(specifications = Interceptor.class)
@Instantiate
public class CachedActionInterceptor extends Interceptor<Cached> {

    @Requires
    protected Cache cache;


    private static final Logger LOGGER = LoggerFactory.getLogger(CachedActionInterceptor.class);

    /**
     * Intercepts a @Cached action method.
     * If the result of the action is cached, returned it immediately without having actually invoked the action method.
     * In this case, the interception chain is cut.
     * <p>
     * If the result is not yet cached, the interception chain continues, and the result is cached to be used during
     * the next invocation.
     *
     * @param configuration the interception configuration
     * @param context       the interception context
     * @return the result.
     * @throws Exception something bad happened
     */
    @Override
    public Result call(Cached configuration, RequestContext context) throws Exception {
        // Can we use the Cached version ?
        boolean nocache =
                HeaderNames.NOCACHE_VALUE.equalsIgnoreCase(context.context().header(HeaderNames.CACHE_CONTROL));

        String key;
        if (Strings.isNullOrEmpty(configuration.key())) {
            key = context.request().uri();
        } else {
            key = configuration.key();
        }

        Result result = null;
        if (!nocache) {
            result = (Result) cache.get(key);
        }

        if (result == null) {
            result = context.proceed();
        } else {
            LOGGER.info("Returning cached result for {} (key:{})",
                    context.request().uri(), key);
            return result;
        }

        Duration duration;
        if (configuration.duration() == 0) {
            // Eternity == 1 year.
            duration = Duration.standardDays(365);
        } else {
            duration = Duration.standardSeconds(configuration.duration());
        }


        cache.set(key, result, duration);
        LoggerFactory.getLogger(this.getClass()).info("Caching result of {} for {} seconds (key:{})",
                context.request().uri(), configuration.duration(), key);

        return result;
    }

    /**
     * @return the cached annotation class.
     */
    @Override
    public Class<Cached> annotation() {
        return Cached.class;
    }
}
