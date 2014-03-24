package org.wisdom.samples.filters;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.util.regex.Pattern;

/**
 * Created by clement on 18/03/2014.
 */
@Component
@Provides
@Instantiate
public class MyUnknownRouteFilter implements Filter {

    @Override
    public Result call(Route route, RequestContext context) throws Throwable {
        System.out.println("Filter called...");
        Result result = context.proceed();
        System.out.println("==>" + result);
        if (result.getStatusCode() == 404) {
            LoggerFactory.getLogger(MyUnknownRouteFilter.class).info("Route " + route.getUrl() + " not found");

            // return Results.notFound("<h1>Sorry guy, nobody here...</h1>");
            return Results.redirect("/samples");
        }
        return result;
    }

    @Override
    public Pattern uri() {
        return Pattern.compile("/samples/.*");
    }

    @Override
    public int priority() {
        return 1000;
    }
}
