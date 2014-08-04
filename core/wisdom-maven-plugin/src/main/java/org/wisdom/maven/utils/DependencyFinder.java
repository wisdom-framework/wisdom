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

import com.google.common.base.Preconditions;
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
public final class DependencyFinder {

    private DependencyFinder() {
        // Avoid direct instantiation.
    }

    /**
     * Gets the file of the dependency with the given artifact id from the plugin dependencies (i.e. from the
     * dependencies of the wisdom-maven-plugin itself, and not from the current 'under-build' project).
     *
     * @param mojo       the mojo, cannot be {@code null}
     * @param artifactId the name of the artifact to find, cannot be {@code null}
     * @param type  the extension of the artifact to find, should not be {@code null}
     * @return the artifact file, {@code null} if not found
     */
    public static File getArtifactFileFromPluginDependencies(AbstractWisdomMojo mojo, String artifactId, String type) {

        Preconditions.checkNotNull(mojo);
        Preconditions.checkNotNull(artifactId);
        Preconditions.checkNotNull(type);

        for (Artifact artifact : mojo.pluginDependencies) {
            if (artifact.getArtifactId().equals(artifactId) && artifact.getType().equals(type)) {
                return artifact.getFile();
            }
        }
        return null;
    }

    /**
     * Gets the file of the dependency with the given artifact id from the project dependencies.
     *
     * @param mojo       the mojo
     * @param artifactId the name of the artifact to find
     * @param type  the extension of the artifact to find
     * @return the artifact file, {@code null} if not found
     */
    public static File getArtifactFileFromProjectDependencies(AbstractWisdomMojo mojo, String artifactId,
                                                              String type) {
        Preconditions.checkNotNull(mojo);
        Preconditions.checkNotNull(artifactId);
        Preconditions.checkNotNull(type);

        // Get artifacts also resolve transitive dependencies.
        for (Artifact artifact : mojo.project.getArtifacts()) {
            if (artifact.getArtifactId().equals(artifactId) && artifact.getType().equals(type)) {
                return artifact.getFile();
            }
        }
        return null;
    }

    /**
     * Gets the file of the dependency with the given artifact id from the project dependencies and if not found from
     * the plugin dependencies. This method also check the extension.
     *
     * @param mojo       the mojo
     * @param artifactId the name of the artifact to find
     * @param type  the extension of the artifact to find
     * @return the artifact file, null if not found
     */
    public static File getArtifactFile(AbstractWisdomMojo mojo, String artifactId, String type) {
        File file = getArtifactFileFromProjectDependencies(mojo, artifactId, type);
        if (file == null) {
            file = getArtifactFileFromPluginDependencies(mojo, artifactId, type);
        }
        return file;
    }

    /**
     * Resolves the specified artifact (using its GAV, classifier and packaging).
     *
     * @param mojo       the mojo
     * @param groupId    the groupId of the artifact to resolve
     * @param artifactId the artifactId of the artifact to resolve
     * @param version    the version
     * @param type       the type
     * @param classifier the classifier
     * @return the artifact's file if it can be revolved. The file is located in the local maven repository.
     * @throws MojoExecutionException if the artifact cannot be resolved
     */
    public static File resolve(AbstractWisdomMojo mojo, String groupId, String artifactId, String version,
                               String type, String classifier) throws MojoExecutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(
                new DefaultArtifact(groupId, artifactId, classifier, type, version));
        request.setRepositories(mojo.remoteRepos);

        mojo.getLog().info("Resolving artifact " + artifactId +
                " from " + mojo.remoteRepos);

        ArtifactResult result;
        try {
            result = mojo.repoSystem.resolveArtifact(mojo.repoSession, request);
        } catch (ArtifactResolutionException e) {
            mojo.getLog().error("Cannot resolve " + groupId + ":" + artifactId + ":" + version + ":" + type);
            throw new MojoExecutionException(e.getMessage(), e);
        }

        mojo.getLog().info("Resolved artifact " + artifactId + " to " +
                result.getArtifact().getFile() + " from "
                + result.getRepository());

        return result.getArtifact().getFile();
    }

    /**
     * Resolves the specified artifact (using the : separated syntax).
     *
     * @param mojo   the mojo
     * @param coords the coordinates ot the artifact to resolve using the : separated syntax -
     *               {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}
     * @return the artifact's file if it can be revolved. The file is located in the local maven repository.
     * @throws MojoExecutionException if the artifact cannot be resolved
     */
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
