package controllers;


import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.cache.Cached;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.text.DateFormat;
import java.util.Date;

@Controller
@Cached(key="cached", duration = 60)
public class CachedController extends DefaultController {

    @Route(method= HttpMethod.GET, uri = "/cached")
    public Result cached() {
        String s = DateFormat.getDateTimeInstance().format(new Date());
        return ok(s);
    }

}
