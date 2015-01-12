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
package snippets.controllers.cache;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
@Path("/cache")
public class CacheUsage extends DefaultController {
    // tag::cache[]
    // Inject the cache service.
    @Requires
    private Cache cache;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result action() {
        // Retrieve an object from the cache
        Object cached = cache.get("my.cache.key");
        if (cached == null) {
            cached = new Object();
            // Store an object in the cache for a specific duration (in second)
            cache.set("my.cache.key", cached, 60);
        }
        return ok();
    }
    // end::cache[]


    public void example() {
        News news = new News();
        // tag::cache-set[]
        cache.set("item.key", news, 60 * 15);
        // end::cache-set[]

        // tag::cache-get[]
        News cached = cache.get("item.key");
        // end::cache-get[]

        // tag::cache-remove[]
        cache.remove("item.key");
        // end::cache-remove[]
    }

    private class News {
    }
    // end::cache-example[]

}
