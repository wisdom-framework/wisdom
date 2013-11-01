package org.ow2.chameleon.wisdom.samples.ajax;


import org.junit.Test;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.samples.helpers.Action;
import org.ow2.chameleon.wisdom.samples.helpers.ControllerTest;
import org.ow2.chameleon.wisdom.samples.helpers.Invocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ow2.chameleon.wisdom.samples.helpers.Action.action;

/**
 * Test the TodoList controller
 */
public class TodoListControllerTest extends ControllerTest {

    TodoListController controller = controller(TodoListController.class)
            .with("router", router())
            .build();


    @Test
    public void testTaskCreation() throws Exception {
       Action.ActionResult result = action(new Invocation() {
           @Override
           public Result invoke() {
               return controller.create("foo");
           }
       }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo(MimeTypes.JSON);
        assertThat(json(result).get("name").textValue()).isEqualTo("foo");
        assertThat(json(result).get("id").asInt()).isInstanceOf(Integer.class);
        assertThat(json(result).get("completed").booleanValue()).isFalse();
    }



    @Test
    public void testTaskDeletion() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.create("foo");
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo(MimeTypes.JSON);
        assertThat(json(result).get("name").textValue()).isEqualTo("foo");
        assertThat(json(result).get("completed").booleanValue()).isFalse();

        final int id = json(result).get("id").intValue();
        result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.delete(id);
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void testTaskRetrieval() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo(MimeTypes.JSON);
        assertThat(jsonarray(result).arrayNode().size()).isEqualTo(0);

        result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.create("foo");
            }
        }).with().attribute("name", "foo").invoke();

        assertThat(status(result)).isEqualTo(OK);

        result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo(MimeTypes.JSON);
        assertThat(jsonarray(result).size()).isEqualTo(1);
    }
}
