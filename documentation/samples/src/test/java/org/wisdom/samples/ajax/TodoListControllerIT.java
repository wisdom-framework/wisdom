/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.samples.ajax;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.test.assertions.ActionResultAssert;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.ControllerTest;
import org.wisdom.test.parents.Invocation;

import javax.inject.Inject;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 *
 */
public class TodoListControllerIT extends ControllerTest {

    @Inject
    TodoListController controller;

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        // Reset List<Task> items
        Field items = controller.getClass().getDeclaredField("items");
        items.setAccessible(true);
        items.set(controller, Lists.newArrayList());
    }

    @Test
    public void testTaskCreation() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.create("foo");
            }
        }).invoke();

        ActionResultAssert.assertThat(result)
                .hasStatus(OK)
                .hasContentType(MimeTypes.JSON);

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
