package controllers.cache;


import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.cache.Cached;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.text.DateFormat;
import java.util.Date;

@Controller
public class CachedController extends DefaultController {

    // tag::cached-action[]
    @Route(method= HttpMethod.GET, uri = "/cached")
    @Cached(key="cached", duration = 60)
    public Result cached() {
        String s = DateFormat.getDateTimeInstance().format(new Date());
        return ok(s);
    }
    // end::cached-action[]

}
