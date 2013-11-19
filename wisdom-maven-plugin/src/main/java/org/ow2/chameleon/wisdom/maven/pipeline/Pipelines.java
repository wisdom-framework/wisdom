package org.ow2.chameleon.wisdom.maven.pipeline;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;

/**
 * Pipeline bootstrap
 */
public class Pipelines {

    public static Pipeline watchers(MavenSession session, Mojo mojo) {
        return new Pipeline(mojo, Watchers.all(session));
    }
}
