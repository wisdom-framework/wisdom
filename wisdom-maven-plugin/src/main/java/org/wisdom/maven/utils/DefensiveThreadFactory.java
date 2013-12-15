package org.wisdom.maven.utils;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A thread factory wrapping the runnable in a defensive
 */
public class DefensiveThreadFactory implements ThreadFactory {
    private final ThreadFactory factory;
    private final String prefix;
    private final Log log;

    public DefensiveThreadFactory(String name, Mojo mojo) {
        factory = Executors.defaultThreadFactory();
        prefix = name;
        log = mojo.getLog();
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapped = new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable e) { //NOSONAR
                    log.error("Error while executing " + Thread.currentThread().getName(), e);
                }
            }
        };
        Thread thread = factory.newThread(wrapped);
        thread.setName(prefix + "-" + thread.getName());
        return thread;
    }
}
