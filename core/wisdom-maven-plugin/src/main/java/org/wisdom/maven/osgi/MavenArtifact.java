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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * An implementation of {@link org.apache.maven.artifact.Artifact} used to recreate {@code Artifact} object from the
 * json form.
 */
public class MavenArtifact extends DefaultArtifact {

    private static final String FAKE = "__";

    /**
     * Constructor used by Jackson.
     */
    public MavenArtifact() {
        super(FAKE, FAKE, "0.0.0", "compile", "jar", "", null);
    }

    /**
     * Ensures that the given artifact was correctly populated.
     *
     * @param artifact the artifact
     * @return {@code true} if the artifact is valid, @{code false} otherwise.
     */
    public static boolean isValid(Artifact artifact) {
        return artifact.getGroupId() != null && !FAKE.equals(artifact.getGroupId());
    }

    /**
     * Removes non valid artifacts from the given list.
     *
     * @param artifacts the list of artifacts than may contain fake artifacts.
     * @return the cleaned up list
     */
    public static Collection<? extends Artifact> filter(Collection<Artifact> artifacts) {
        LinkedHashSet<Artifact> set = new LinkedHashSet<>();
        for (Artifact artifact : artifacts) {
            if (MavenArtifact.isValid(artifact)) {
                set.add(artifact);
            }
        }
        return set;
    }

    /**
     * A setter method made especially for Jackson. Artifact's dependency filter is used for the exclusion list. As
     * the filter cannot be created directly by Jackson, we do the job directly here.
     *
     * @param filter the Json representation of the filter
     */
    public void setDependencyFilter(ObjectNode filter) {
        if (filter == null) {
            return;
        }
        final JsonNode patterns = filter.get("patterns");
        if (patterns != null && patterns.isArray()) {
            List<String> ids = new ArrayList<>();
            for (JsonNode exclusion : patterns) {
                ids.add(exclusion.asText());
            }
            setDependencyFilter(new ExcludesArtifactFilter(ids));
        }
    }
}
