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
package org.wisdom.maven.mojos;

import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.*;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mojo running a 'watched' instance of Wisdom. It deploys the applications and monitor for changes. On each change,
 * the Maven 'watch' pipeline is triggered to re-deploy the bundle, or update configurations and files.
 */
@Mojo(name = "run", threadSafe = false,
        // We need to use the TEST scope to let Surefire access its dependencies.
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true
)
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractWisdomMojo implements Contextualizable {

    /**
     * Enables / disables the pom file monitoring.
     */
    @Parameter(defaultValue = "${pom.watching}")
    protected boolean pomFileMonitoring = true;

    /**
     * The last modification date of the project's pom.xml file. This field is required on Windows as the exit code
     * and not propagated correctly.
     */
    private long lastPomFileModification;

    /**
     * Component used to build the Maven project from the pom file.
     */
    @Component
    private ProjectBuilder projectBuilder;

    /**
     * The component used to execute the second Maven execution.
     */
    @Component
    private LifecycleExecutor lifecycleExecutor;

    /**
     * The plexus container.
     */
    private PlexusContainer container;

    /**
     * Sets to false to indicate that we are executing a nested 'build'.
     */
    private boolean initialBuild = true;

    @Override
    public void execute() throws MojoExecutionException {
        File pom = project.getFile();
        lastPomFileModification = pom.lastModified();

        MavenProject newProject = null;
        try {
            final ProjectBuildingResult result = loadMavenProject();
            newProject = result.getProject();
        } catch (ProjectBuildingException exception) {
            getLog().error("Error(s) detected in the pom file: " + exception.getMessage());
            if (initialBuild) {
                throw new MojoExecutionException("Invalid pom file, check log", exception);
            }
            if (pomFileMonitoring) {
                waitForModification();
                execute();
            }
        }

        // newProject is necessarily set here and valid.

        MavenExecutionRequest execRequest = getMavenExecutionRequest();
        MavenSession newSession = getMavenSession(newProject, execRequest); //NOSONAR
        // The session is going to be cleared, write the watcher list in the container.
//        container.getContext().put(Watchers.WATCHERS_KEY, Watchers.all(session));
        initialBuild = false;

        try {
            lifecycleExecutor.execute(newSession);
        } catch (RuntimeException e) {
            // Maven may throw internal error, they are fatal, stop everything.
            getLog().info("Maven has thrown a fatal error, stopping the watch mode: " + e.getMessage());
            return;
        }

        if (newSession.getResult().hasExceptions()) {
            getLog().error("Exception(s) detected while launching Maven : " + newSession.getResult().getExceptions());
            if (initialBuild) {
                throw new MojoExecutionException("Exception(s) detected while launching Maven, check log",
                        newSession.getResult().getExceptions().get(0));
            }
            if (pomFileMonitoring) {
                waitForModification();
                execute();
            }
            return;
        }

        if (pomFileMonitoring && pomFileModified()) {
            execute();
        }
    }

    /**
     * Waits for pom file modification. This method is used when a pom file has problems,
     * to reload the project when they are fixed.
     */
    private void waitForModification() {
        while( ! pomFileModified()) {
            try {
                Thread.sleep(Integer.getInteger("watch.period", 2) * 1000);
            } catch (InterruptedException e) {
                // Ignore it.
            }
        }
    }

    private MavenSession getMavenSession(final MavenProject project, MavenExecutionRequest request) {
        MavenSession newSession = new MavenSession(container,
                session.getRepositorySession(),
                request,
                session.getResult());
        newSession.setAllProjects(session.getAllProjects());
        newSession.setCurrentProject(project);
        newSession.setParallel(session.isParallel());
        // Update project map to update the current project
        Map<String, MavenProject> projectMaps = new LinkedHashMap<>(session.getProjectMap());
        projectMaps.put(ArtifactUtils.key(project.getGroupId(), project.getArtifactId(),
                project.getVersion()), project);
        newSession.setProjectMap(projectMaps);

        /**
         * Fake implementation of the project dependency graph, as we don't support reactor.
         */
        ProjectDependencyGraph graph = new ProjectDependencyGraph() {

            @Override
            public List<MavenProject> getSortedProjects() {
                return ImmutableList.of(project);
            }

            @Override
            public List<MavenProject> getDownstreamProjects(MavenProject project, boolean transitive) {
                return Collections.emptyList();
            }

            @Override
            public List<MavenProject> getUpstreamProjects(MavenProject project, boolean transitive) {
                return Collections.emptyList();
            }
        };
        newSession.setProjectDependencyGraph(graph);
        newSession.setProjects(ImmutableList.of(project));
        return newSession;
    }


    private MavenExecutionRequest getMavenExecutionRequest() {
        MavenExecutionRequest request = DefaultMavenExecutionRequest.copy(session.getRequest());
        request.setStartTime(session.getStartTime());
        request.setExecutionListener(null);
        if (! initialBuild  && session.getGoals().contains("clean")) {
            // Here the package phase is required to restore the runtime environment
            request.setGoals(ImmutableList.of("clean", "package", "wisdom:internal-run"));
        } else {
            // It is safer to re-execute the package phase to have the new classes...
            request.setGoals(ImmutableList.of("package", "wisdom:internal-run"));
        }
        return request;
    }

    private ProjectBuildingResult loadMavenProject() throws ProjectBuildingException {
        DefaultProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        request.setRepositorySession(repoSession);
        request.setUserProperties(session.getUserProperties());
        request.setSystemProperties(session.getSystemProperties());
        request.setProfiles(session.getRequest().getProfiles());
        request.setActiveProfileIds(session.getRequest().getActiveProfiles());
        request.setRemoteRepositories(session.getRequest().getRemoteRepositories());
        request.setBuildStartTime(session.getRequest().getStartTime());
        request.setInactiveProfileIds(session.getRequest().getInactiveProfiles());
        request.setPluginArtifactRepositories(session.getRequest().getPluginArtifactRepositories());
        request.setLocalRepository(session.getRequest().getLocalRepository());

        return projectBuilder.build(project.getFile(), request);

    }

    /**
     * On windows we can't play with the exit code, so check for the pom file modification dates.
     *
     * @return {@code true} if the pom file was modified. {@code false} otherwise.
     */
    private boolean pomFileModified() {
        File pom = project.getFile();
        return pom.lastModified() > lastPomFileModification;
    }

    /**
     * Retrieves the Plexus container.
     * @param context the context
     * @throws ContextException if the container cannot be retrieved.
     */
    @Override
    public void contextualize(Context context) throws ContextException {
        container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }
}
