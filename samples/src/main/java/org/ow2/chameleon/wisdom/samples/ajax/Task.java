package org.ow2.chameleon.wisdom.samples.ajax;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a task
 */
public class Task {

    public final String name;
    public final int id;
    public boolean completed = false;

    public static AtomicInteger counter = new AtomicInteger();


    public String update;
    public String delete;

    public Task(String name) {
        this.id = counter.getAndIncrement();
        this.name = name;
    }

    public void completed(boolean completed) {
        this.completed = completed;
    }

    public void setUpdateUrl(String u) {
        this.update = u;
    }

    public void setDeleteUrl(String u) {
        this.delete = u;
    }
}
