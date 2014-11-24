package controllers.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.util.regex.Pattern;

@Service
public class MyDummyFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyDummyFilter.class);

    @Override
    public Result call(Route route, RequestContext context) throws Exception {
        LOGGER.info("Request intercepted by {}", this);
        if (context.request().parameterAsBoolean("insertValue")) {
            context.data().put("key", "value");
        }
        Result result = context.proceed();
        result.getHeaders().put("X-Filtered", "true");
        return result;
    }

    @Override
    public Pattern uri() {
        return Pattern.compile("/filter/dummy");
    }

    @Override
    public int priority() {
        return 1000;
    }
}
