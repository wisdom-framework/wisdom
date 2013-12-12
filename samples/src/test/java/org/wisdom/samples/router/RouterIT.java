package org.wisdom.samples.router;

import org.junit.Test;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.Router;
import org.wisdom.samples.hello.HelloController;
import org.wisdom.samples.hello.MyForm;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * Check router.
 */
public class RouterIT extends WisdomTest {

    @Inject
    public Router router;

    @Test
    public void testSampleRoutes() throws Throwable {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                try {
                    System.out.println("route : " + router.getRouteFor(HttpMethod.GET, "/samples/hello/"));
                    return router.getRouteFor(HttpMethod.GET, "/samples/hello/").invoke();
                } catch(Throwable e) {
                    e.printStackTrace();
                    return Results.internalServerError(e);
                }
            }
        }).invoke();
        System.out.println(toString(result));
        assertThat(status(result)).isEqualTo(OK);

    }

    @Test
    public void testHelloRoute() throws Throwable {
        MyForm myform = new MyForm();
        myform.name = "--wisdom--";
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return router.getRouteFor(HttpMethod.POST, "/samples/hello/result").invoke();
            }
        }).with().body(myform).invoke();
        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).contains("Hello", "--wisdom--");
    }

}
