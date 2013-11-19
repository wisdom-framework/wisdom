package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.maven.execution.MavenSession;
import org.ow2.chameleon.wisdom.maven.Watcher;
import org.ow2.chameleon.wisdom.maven.pipeline.Watchers;

/**
 * Common part.
 */
public abstract class AbstractWisdomWatcherMojo extends AbstractWisdomMojo implements Watcher {

    public void setSession(MavenSession session) {
        this.session = session;
        Watchers.add(session, this);
    }

}
