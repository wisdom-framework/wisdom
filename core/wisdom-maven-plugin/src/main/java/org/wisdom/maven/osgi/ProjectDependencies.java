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
package org.wisdom.maven.osgi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A structure storing the project dependencies (direct and transitive). This class is made to ease the storing and
 * reloading in / from JSON.
 */
public class ProjectDependencies {

    private final Set<Artifact> directDependencies = new LinkedHashSet<>();

    private final Set<Artifact> transitiveDependencies = new LinkedHashSet<>();

    /**
     * Creates the project dependencies instance.
     *
     * @param direct     the direct dependencies
     * @param transitive the transitive dependencies
     */
    @JsonCreator
    public ProjectDependencies(
            @JsonProperty("directDependencies") Collection<Artifact> direct,
            @JsonProperty("transitiveDependencies") Collection<Artifact> transitive) {
        if (direct != null) {
            directDependencies.addAll(MavenArtifact.filter(direct));
        }
        if (transitiveDependencies != null) {
            transitiveDependencies.addAll(MavenArtifact.filter(transitive));
        }
    }

    /**
     * Creates the project dependencies instance from a Maven Project.
     *
     * @param project the maven project
     */
    public ProjectDependencies(MavenProject project) {
        this(project.getDependencyArtifacts(), project.getArtifacts());
    }

    /**
     * Gets the set of artifact forming the direct dependency set.
     *
     * @return the set of direct dependencies
     */
    @JsonDeserialize(contentAs = MavenArtifact.class)
    public Set<Artifact> getDirectDependencies() {
        return directDependencies;
    }

    /**
     * Gets the set of artifact forming the transitive dependency set.
     *
     * @return the set of transitive dependencies
     */
    @JsonDeserialize(contentAs = MavenArtifact.class)
    public Set<Artifact> getTransitiveDependencies() {
        return transitiveDependencies;
    }
}
