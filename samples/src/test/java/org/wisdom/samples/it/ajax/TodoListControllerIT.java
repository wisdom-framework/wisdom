package org.wisdom.samples.it.ajax;

import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.samples.ajax.TodoListController;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.ControllerTest;
import org.wisdom.test.parents.Invocation;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 *
 */
public class TodoListControllerIT extends ControllerTest {

    @Inject
    TodoListController controller;

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
        result = Action.action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.delete(id);
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void testTaskRetrieval() throws Exception {
        Action.ActionResult result = Action.action(new Invocation() {
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

        result = Action.action(new Invocation() {
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
