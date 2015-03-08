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
package org.wisdom.maven.pipeline;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.wisdom.maven.Watcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the watchers.
 */
public final class Watchers {

    /**
     * The key in which the watcher list is stored.
     */
    public static final String WATCHERS_KEY = "WATCHERS";

    /**
     * Registers a watcher.
     *
     * @param session the Maven session
     * @param watcher the watcher to add
     */
    public static synchronized void add(MavenSession session, Watcher watcher) {
        get(session).add(watcher);
    }

    /**
     * Registers a watcher.
     *
     * @param context the Plexus context
     * @param watcher the watcher to add
     */
    public static synchronized void add(Context context, Watcher watcher) {
        get(context).add(watcher);
    }

    /**
     * Un-registers a watcher.
     *
     * @param session the Maven session
     * @param watcher the watcher to remove
     * @return {@literal true} if the watcher was removed, {@literal false} otherwise.
     */
    public static synchronized boolean remove(MavenSession session, Watcher watcher) {
        return !(session == null || watcher == null) && get(session).remove(watcher);
    }

    /**
     * Checks whether the current watcher list from the given MavenSession contains the given watcher.
     *
     * @param session the Maven session
     * @param watcher the watcher
     * @return {@literal true} if the session contains the given watcher, {@literal false} otherwise.
     */
    public static synchronized boolean contains(MavenSession session, Watcher watcher) {
        return get(session).contains(watcher);
    }

    /**
     * Gets the list of watchers from the given MavenSession.
     *
     * @param session the Maven session
     * @return the list of watcher, empty if none. Modifying the resulting list, updates the stored list.
     */
    static synchronized List<Watcher> get(MavenSession session) {
        List<Watcher> watchers = (List<Watcher>) session.getExecutionProperties().get(WATCHERS_KEY);
        if (watchers == null) {
            watchers = new ArrayList<>();
            session.getExecutionProperties().put(WATCHERS_KEY, watchers);
        }
        return watchers;
    }

    /**
     * Gets the list of watchers from the given Plexus context.
     *
     * @param context the Plexus context
     * @return the list of watcher, empty if none. Modifying the resulting list, updates the stored list.
     */
    static synchronized List<Watcher> get(Context context) {
        List<Watcher> watchers;
        if (context.contains(WATCHERS_KEY)) {
            try {
                watchers = (List<Watcher>) context.get(WATCHERS_KEY);
            } catch (ContextException e) {
                throw new IllegalStateException("Cannot extract the watcher from the context", e);
            }
        } else {
            watchers = new ArrayList<>();
            context.put(WATCHERS_KEY, watchers);
        }
        return watchers;
    }

    /**
     * Gets a copy of the list of watchers from the given MavenSession.
     *
     * @param session the Maven session
     * @return a copy of the watcher list, empty if none.
     */
    public static synchronized List<Watcher> all(MavenSession session) {
        return new ArrayList<>(get(session));
    }

    /**
     * Gets a copy of the list of watchers from the given Plexus context.
     *
     * @param context the Plexus context
     * @return a copy of the watcher list, empty if none.
     */
    public static synchronized List<Watcher> all(Context context) {
        return new ArrayList<>(get(context));
    }

    private Watchers() {
        // Avoid direct instantiation.
    }

}
