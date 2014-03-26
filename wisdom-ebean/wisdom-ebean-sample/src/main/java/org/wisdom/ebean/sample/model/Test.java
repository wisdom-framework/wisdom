package org.wisdom.ebean.sample.model;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.wisdom.api.annotations.Model;
import org.wisdom.api.model.Crud;
import org.wisdom.api.model.EntityFilter;

import java.util.List;

/**
 * Created by clement on 19/02/2014.
 */
@Component
@Instantiate
public class Test {

    @Model(Task.class)
    Crud<Task, Long> tasks;

    @Validate
    public void start() {
        System.out.println("Starting... " + tasks);
        Task task = new Task();
        task.name = "bla";
        task = tasks.save(task);
        System.out.println("Saved : " + task.name + " / " + task.id);

        task = new Task();
        task.name = "foo";
        task = tasks.save(task);
        System.out.println("Saved : " + task.name + " / " + task.id);

        task = new Task();
        task.name = "bar";
        task = tasks.save(task);
        System.out.println("Saved : " + task.name + " / " + task.id);

        Iterable<Task> list = tasks.findAll();
        for (Task t : list) {
            System.out.println("Queried task : " + t.id + " : " + t.name);
            t.done = true;
            tasks.save(t);
        }


        list = tasks.findAll(new EntityFilter<Task>() {
            @Override
            public boolean accept(Task task) {
                return task.done;
            }
        });
        for (Task t : list) {
            System.out.println("Completed Queried task : " + t.id + " : " + t.name);
            t.done = true;
            tasks.save(t);
        }

    }


}
