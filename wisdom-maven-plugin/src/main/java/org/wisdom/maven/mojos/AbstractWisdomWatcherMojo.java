package org.wisdom.maven.mojos;

import org.apache.maven.execution.MavenSession;
import org.wisdom.maven.Watcher;
import org.wisdom.maven.pipeline.Watchers;

/**
 * Common part.
 */
public abstract class AbstractWisdomWatcherMojo extends AbstractWisdomMojo implements Watcher {

    public void setSession(MavenSession session) {
        this.session = session;
        Watchers.add(session, this);
    }

    public void removeFromWatching() {
        Watchers.remove(session, this);
    }

}
