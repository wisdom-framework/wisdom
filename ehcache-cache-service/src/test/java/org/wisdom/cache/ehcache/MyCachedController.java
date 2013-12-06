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
        Integer r = (Integer) cache.get("result");
        if (r == null) {
            r = 100;
            cache.set("result", 101, Duration.standardSeconds(2));
        }
        return ok(Integer.toString(r));
    }
}
