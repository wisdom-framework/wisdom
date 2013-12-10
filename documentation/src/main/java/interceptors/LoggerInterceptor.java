package interceptors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Result;
import org.wisdom.api.interceptor.InterceptionContext;
import org.wisdom.api.interceptor.Interceptor;

@Component
@Provides(specifications = Interceptor.class)
@Instantiate
public class LoggerInterceptor extends Interceptor<Logged> {

    Logger logger = LoggerFactory.getLogger(LoggerInterceptor.class);

    @Override
    public Result call(Logged configuration, InterceptionContext context)
            throws Throwable {
        logger.info("Invoking " + context.context().request().method() +
                " " + context.context().request().uri());
        long begin = System.currentTimeMillis();

        Result result = context.proceed();

        long end = System.currentTimeMillis();
        if (configuration.duration()) {
            logger.info("Result computed in " + (end - begin) + " ms");
        }

        return result;
    }

    @Override
    public Class<Logged> annotation() {
        return Logged.class;
    }
}
