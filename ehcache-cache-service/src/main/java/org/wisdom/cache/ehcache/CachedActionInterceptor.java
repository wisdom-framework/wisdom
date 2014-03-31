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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.joda.time.Duration;
import org.slf4j.LoggerFactory;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.cache.Cached;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.interception.Interceptor;

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
    public Result call(Cached configuration, RequestContext context) throws Exception {
        Result result = (Result) cache.get(configuration.key());

        if (result == null) {
            result = context.proceed();
            Duration duration;
            if (configuration.duration() == 0) {
                // Eternity == 1 year.
                duration = Duration.standardDays(365);
            } else {
                duration = Duration.standardSeconds(configuration.duration());
            }
            cache.set(configuration.key(), result, duration);
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
