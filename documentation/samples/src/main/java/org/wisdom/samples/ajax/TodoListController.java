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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.util.List;

/**
 * A simple controller to manage a todo list (in memory).
 */
@Controller
public class TodoListController extends DefaultController {

    @View("ajax/index")
    private Template index;
    @Requires
    private Router router;
    private List<Task> items = Lists.newArrayList();

    @Route(method = HttpMethod.GET, uri = "/todo")
    public Result index() {
        return ok(render(index));
    }

    @Route(method = HttpMethod.GET, uri = "/todo/tasks")
    public Result retrieve() {
        return ok(items).json();
    }

    @Route(method = HttpMethod.DELETE, uri = "/todo/tasks/{id}")
    public Result delete(@Parameter("id") int id) {
        removeTaskById(id);
        return ok();
    }

    @Route(method = HttpMethod.POST, uri = "/todo/tasks")
    public Result create(@FormParameter("name") String name) {
        Task task = new Task(name);
        task.setUpdateUrl(router.getReverseRouteFor(this, "update", "id", task.id));
        task.setDeleteUrl(router.getReverseRouteFor(this, "delete", "id", task.id));
        items.add(task);
        return ok(task).json();
    }

    @Route(method = HttpMethod.POST, uri = "/todo/tasks/{id}")
    public Result update(@Parameter("id") int id, @FormParameter("completed") boolean completed) {
        Task task = getTaskById(id);
        if (task == null) {
            return notFound().render(ImmutableMap.of("message", "Task " + context().parameterFromPath
                    ("id") + " not found")).json();
        } else {
            task.completed(completed);
            if (completed) {
                return ok().render(ImmutableMap.of("message", "Task " + context().parameterFromPath
                        ("id") + " completed")).json();
            } else {
                return ok().render(ImmutableMap.of("message", "Task " + context().parameterFromPath
                        ("id") + " uncompleted")).json();
            }
        }
    }

    private Task getTaskById(int id) {
        for (Task t : items) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }

    private void removeTaskById(int id) {
        Task t = getTaskById(id);
        if (t != null) {
            items.remove(t);
        }
    }


}
