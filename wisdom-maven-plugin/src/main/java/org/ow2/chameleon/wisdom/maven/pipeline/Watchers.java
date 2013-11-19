package org.ow2.chameleon.wisdom.maven.pipeline;

import org.apache.maven.execution.MavenSession;
import org.ow2.chameleon.wisdom.maven.Watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the watchers.
 */
public class Watchers {

    public static final String WATCHERS_KEY = "WATCHERS";

    public static synchronized void add(MavenSession session, Watcher watcher) {
        get(session).add(watcher);
    }

    public static synchronized boolean remove(MavenSession session, Watcher watcher) {
        return get(session).remove(watcher);
    }

    public static synchronized boolean contains(MavenSession session, Watcher watcher) {
        return get(session).contains(watcher);
    }

    public static synchronized List<Watcher> get(MavenSession session) {
        List<Watcher> watchers = (List<Watcher>) session.getExecutionProperties().get(WATCHERS_KEY);
        if (watchers == null) {
            watchers = new ArrayList<>();
            session.getExecutionProperties().put(WATCHERS_KEY, watchers);
        }
        return watchers;
    }

    public static synchronized List<Watcher> all(MavenSession session) {
        return new ArrayList<>(get(session));
    }

}
