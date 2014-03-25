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
