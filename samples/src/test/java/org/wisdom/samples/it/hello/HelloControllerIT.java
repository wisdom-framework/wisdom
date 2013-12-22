package org.wisdom.samples.it.hello;

import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.samples.hello.HelloController;
import org.wisdom.samples.hello.MyForm;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.ControllerTest;
import org.wisdom.test.parents.Invocation;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 *
 */
public class HelloControllerIT extends ControllerTest {

    @Inject
    HelloController controller;

    @Test
    public void testIndex() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.index();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).contains("<h3>A service wishing you a good day, sincerely</h3>");
    }

    @Test
    public void testHello() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                MyForm form = new MyForm();
                form.name = "wisdom";
                return controller.hello(form);
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).contains("Hello", "wisdom");
    }

    @Test
    public void testNoForm() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.hello(null);
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testEmptyForm() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                MyForm form = new MyForm();
                return controller.hello(form);
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
    }

}
