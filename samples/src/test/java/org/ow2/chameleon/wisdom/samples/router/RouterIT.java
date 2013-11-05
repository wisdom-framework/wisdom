package org.ow2.chameleon.wisdom.samples.router;

import org.junit.Test;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.samples.hello.MyForm;
import org.ow2.chameleon.wisdom.test.parents.Action;
import org.ow2.chameleon.wisdom.test.parents.Invocation;
import org.ow2.chameleon.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ow2.chameleon.wisdom.test.parents.Action.action;

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
                return router.getRouteFor(HttpMethod.GET, "/samples").invoke();
            }
        }).invoke();
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
