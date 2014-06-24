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
package org.wisdom.maven.utils;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A thread factory wrapping the runnable in a defensive.
 */
public class DefensiveThreadFactory implements ThreadFactory {
    private final ThreadFactory factory;
    private final String prefix;
    private final Log log;

    /**
     * Creates the defensive thread factory.
     *
     * @param name the prefix that will be used for all threads created from this factory.
     * @param mojo the mojo used to retrieve the log object.
     */
    public DefensiveThreadFactory(String name, Mojo mojo) {
        factory = Executors.defaultThreadFactory();
        prefix = name;
        log = mojo.getLog();
    }

    /**
     * Creates a new threads.
     * @param runnable the runnable, should not be {@literal null}.
     * @return the created thread.
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapped = new Runnable() {
            @Override
            public void run() {
                try {
                    // As the runnable is wrapped in another runnable object, we call the run method directly.
                    runnable.run();     //NOSONAR
                } catch (Exception e) {  //NOSONAR
                    // Catching exception is acceptable here as the wrapped runnable can throw almost anything.
                    log.error("Error while executing " + Thread.currentThread().getName(), e);
                }
            }
        };
        Thread thread = factory.newThread(wrapped);
        thread.setName(prefix + "-" + thread.getName());
        return thread;
    }
}
