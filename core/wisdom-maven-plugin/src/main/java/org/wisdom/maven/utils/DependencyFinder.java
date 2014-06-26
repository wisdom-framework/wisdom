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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;

/**
 * Class responsible for finding dependency files in projects and plugins dependency.
 */
public class DependencyFinder {


    /**
     * Gets the file of the dependency with the given artifact id from the plugin dependencies.
     *
     * @return the artifact file, null if not found
     */
    public static File getArtifactFileFromPluginDependencies(AbstractWisdomMojo mojo, String artifactId, String extension) {
        for (Artifact artifact : mojo.pluginDependencies) {
            if (artifact.getArtifactId().equals(artifactId) && artifact.getType().equals(extension)) {
                return artifact.getFile();
            }
        }
        return null;
    }

    /**
     * Gets the file of the dependency with the given artifact id from the project dependencies.
     *
     * @return the artifact file, null if not found
     */
    public static File getArtifactFileFromProjectDependencies(AbstractWisdomMojo mojo, String artifactId,
                                                              String extension) {
        // Get artifacts also resolve transitive dependencies.
        for (Artifact artifact : mojo.project.getArtifacts()) {
            if (artifact.getArtifactId().equals(artifactId) && artifact.getType().equals(extension)) {
                return artifact.getFile();
            }
        }
        return null;
    }

    /**
     * Gets the file of the dependency with the given artifact id from the project dependencies and if not found from
     * the plugin dependencies. This method also check the extension.
     *
     * @return the artifact file, null if not found
     */
    public static File getArtifactFile(AbstractWisdomMojo mojo, String artifactId, String extension) {
        File file = getArtifactFileFromProjectDependencies(mojo, artifactId, extension);
        if (file == null) {
            file = getArtifactFileFromPluginDependencies(mojo, artifactId, extension);
        }
        return file;
    }

    public static File resolve(AbstractWisdomMojo mojo, String groupId, String artifact, String version,
                               String type, String classifier) throws MojoExecutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(
                new DefaultArtifact(groupId, artifact, classifier, type, version));
        request.setRepositories(mojo.remoteRepos);

        mojo.getLog().info("Resolving artifact " + artifact +
                " from " + mojo.remoteRepos);

        ArtifactResult result;
        try {
            result = mojo.repoSystem.resolveArtifact(mojo.repoSession, request);
        } catch (ArtifactResolutionException e) {
            mojo.getLog().error("Cannot resolve " + groupId + ":" + artifact + ":" + version + ":" + type);
            throw new MojoExecutionException(e.getMessage(), e);
        }

        mojo.getLog().info("Resolved artifact " + artifact + " to " +
                result.getArtifact().getFile() + " from "
                + result.getRepository());

        return result.getArtifact().getFile();
    }

    public static File resolve(AbstractWisdomMojo mojo, String coords) throws MojoExecutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(
                new DefaultArtifact(coords));
        request.setRepositories(mojo.remoteRepos);

        mojo.getLog().info("Resolving artifact " + coords +
                " from " + mojo.remoteRepos);

        ArtifactResult result;
        try {
            result = mojo.repoSystem.resolveArtifact(mojo.repoSession, request);
        } catch (ArtifactResolutionException e) {
            mojo.getLog().error("Cannot resolve " + coords);
            throw new MojoExecutionException(e.getMessage(), e);
        }

        mojo.getLog().info("Resolved artifact " + coords + " to " +
                result.getArtifact().getFile() + " from "
                + result.getRepository());

        return result.getArtifact().getFile();
    }


}
