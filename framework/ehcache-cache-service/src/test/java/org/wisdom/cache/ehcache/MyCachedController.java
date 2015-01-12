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
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * A controller using the cache service.
 */
@Component
@Provides(specifications=Controller.class)
@Instantiate
public class MyCachedController extends DefaultController {

    @Requires
    private Cache cache;

    /**
     * @return 100 the first time, 101 the second one.
     */
    @Route(method = HttpMethod.GET, uri = "/cache")
    public Result retrieve() {
        Integer r = cache.get("result");
        if (r == null) {
            r = 100;
            cache.set("result", 101, Duration.standardSeconds(2));
        }
        return ok(Integer.toString(r));
    }
}
