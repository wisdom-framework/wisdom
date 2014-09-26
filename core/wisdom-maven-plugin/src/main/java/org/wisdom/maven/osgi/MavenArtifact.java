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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;

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
}
