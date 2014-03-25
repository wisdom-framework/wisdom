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
import org.wisdom.maven.Watcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the watchers.
 */
public class Watchers {

    public static final String WATCHERS_KEY = "WATCHERS";

    public static synchronized void add(MavenSession session, Watcher watcher) {
        get(session).add(watcher);
    }

    public static synchronized boolean remove(MavenSession session, Watcher watcher) {
        return !(session == null || watcher == null) && get(session).remove(watcher);
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
