package org.ow2.chameleon.wisdom.samples.async;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.Parameter;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.util.concurrent.Callable;

/**
 * A simple controller demonstrating async result.
 */
@Controller
public class SimpleAsyncController extends DefaultController {

    /**
     * Waits ten second before sending the hello message.
     */
    @Route(method = HttpMethod.GET, uri = "/async/hello/{name}")
    public Result heavyComputation(@Parameter("name") final String name) {
        return async(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                System.out.println(System.currentTimeMillis() + " - Heavy computation executed from " + Thread
                        .currentThread().getName());
                Thread.sleep(10000);
                System.out.println(System.currentTimeMillis() + " -  Heavy computation done " + Thread.currentThread
                        ().getName());
                return ok("Hello " + name);
            }
        });
    }
}
