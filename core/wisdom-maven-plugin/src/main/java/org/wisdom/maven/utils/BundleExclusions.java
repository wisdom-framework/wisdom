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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeMultimap;
import org.apache.maven.artifact.Artifact;

import java.util.Set;

/**
 * A list of artifact we don't want to copy. They contain well-known hidden issues and are generally provided by the
 * framework itself.
 */
public class BundleExclusions {

    /**
     * The exclusion set.
     * This set is a multi-map organized as follows: GroupId - ArtifactId*.
     * This structure makes searches in the map quite efficient.
     */
    public static TreeMultimap<String, String> EXCLUSIONS = TreeMultimap.create();

    /**
     * Initializes the exclusion set.
     */
    static {
        // iPOJO Annotations
        EXCLUSIONS.put("org.apache.felix", "org.apache.felix.ipojo.annotations");

        // OSGi
        EXCLUSIONS.putAll("org.osgi", ImmutableList.of("org.osgi.core", "osgi_R4_core", "osgi_R4_compendium", "core"));
        EXCLUSIONS.put("org.apache.felix", "org.osgi.core");

        // SLF4J
        EXCLUSIONS.putAll("org.slf4j", ImmutableList.of("slf4j-api", "slf4j-jcl", "slf4j-nop"));

        // STAX
        EXCLUSIONS.put("stax", "stax-api");
        EXCLUSIONS.put("javax.xml.stream", "stax-api");
        EXCLUSIONS.put("org.codehaus.woodstox", "stax2-api");

        // BND
        EXCLUSIONS.put("biz.aQute.bnd", "bndlib");

        // Provided by Wisdom
        EXCLUSIONS.put("com.google.guava", "guava");
    }

    /**
     * Checks whether the given artifact is on the excluded list.
     *
     * @param artifact the artifact
     * @return {@literal true} if the artifact is excluded, {@literal false} otherwise
     */
    public static boolean isExcluded(Artifact artifact) {
        Set<String> excluded = EXCLUSIONS.get(artifact.getGroupId());
        return excluded != null && excluded.contains(artifact.getArtifactId());
    }
}
