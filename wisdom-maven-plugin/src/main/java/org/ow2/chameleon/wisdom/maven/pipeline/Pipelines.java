package org.ow2.chameleon.wisdom.maven.pipeline;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;

import java.io.File;

/**
 * Pipeline bootstrap
 */
public class Pipelines {

    public static Pipeline watchers(MavenSession session, File baseDir, Mojo mojo) {
        return new Pipeline(mojo, baseDir, Watchers.all(session));
    }
}
