package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.maven.artifact.Artifact;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;

/**
 * Class responsible for finding dependency files in projects and plugins dependency.
 */
public class DependencyFinder {


    /**
     * Gets the file of the dependency with the given artifact id from the plugin dependencies.
     * @return the artifact file, null if not found
     */
    public static File getArtifactFileFromPluginDependencies(AbstractWisdomMojo mojo, String artifactId,
                                                             String extension) {
        for (Artifact artifact : mojo.pluginDependencies) {
            if (artifact.getArtifactId().equals(artifactId)  && artifact.getType().equals(extension)) {
                return artifact.getFile();
            }
        }
        return null;
    }

    /**
     * Gets the file of the dependency with the given artifact id from the project dependencies.
     * @return the artifact file, null if not found
     */
    public static File getArtifactFileFromProjectDependencies(AbstractWisdomMojo mojo, String artifactId,
                                                              String extension) {
        // Get artifacts also resolve transitive dependencies.
        for (Artifact artifact : mojo.project.getArtifacts()) {
            if (artifact.getArtifactId().equals(artifactId)  && artifact.getType().equals(extension)) {
                return artifact.getFile();
            }
        }
        return null;
    }

    /**
     * Gets the file of the dependency with the given artifact id from the project dependencies and if not found from
     * the plugin dependencies. This method also check the extension.
     * @return the artifact file, null if not found
     */
    public static File getArtifactFile(AbstractWisdomMojo mojo, String artifactId, String extension) {
        File file = getArtifactFileFromProjectDependencies(mojo, artifactId, extension);
        if (file == null) {
            file = getArtifactFileFromPluginDependencies(mojo, artifactId, extension);
        }
        return file;
    }


}
