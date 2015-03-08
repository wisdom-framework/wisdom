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

import aQute.bnd.osgi.Constants;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * A class responsible for computing the instructions for the `Embed-Dependency` clauses.
 */
public class DependencyEmbedder {

    /**
     * Embed Dependency instruction.
     */
    public static final String EMBED_DEPENDENCY = "Embed-Dependency";

    /**
     * Embed Transitive instruction.
     */
    public static final String EMBED_TRANSITIVE = "Embed-Transitive";

    private final boolean embedTransitive;
    private final Reporter reporter;

    private List<EmbeddedDependency> embedded = new ArrayList<>();

    /**
     * Creates an instance of {@link org.wisdom.maven.osgi.DependencyEmbedder}.
     *
     * @param instructions the current set of instructions
     * @param reporter     the reporter
     */
    public DependencyEmbedder(Properties instructions, Reporter reporter) {
        this.reporter = reporter;
        String embedDependencyHeader = instructions.getProperty(EMBED_DEPENDENCY);
        embedTransitive = "true".equalsIgnoreCase(instructions.getProperty(EMBED_TRANSITIVE));
        if (!Strings.isNullOrEmpty(embedDependencyHeader)) {
            parse(embedDependencyHeader);
        }

    }

    private void parse(String embedDependencyHeader) {
        List<String> inst = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(embedDependencyHeader);
        for (String s : inst) {
            embedded.add(parseIndividual(s));
        }
    }

    private EmbeddedDependency parseIndividual(String s) {
        List<String> segments = Splitter.on(";").omitEmptyStrings().trimResults().splitToList(s);
        if (segments.isEmpty()) {
            // Broken instruction
            reporter.error("Incorrect Embed-Dependency clause : " + s);
            return null;
        }

        EmbeddedDependency configuration = new EmbeddedDependency(s);
        AndArtifactFilter filter = new AndArtifactFilter();
        configuration.setFilter(filter);

        filter.add(new PatternIncludesArtifactFilter(ImmutableList.of(segments.get(0)), embedTransitive));
        for (int i = 1; i < segments.size(); i++) {
            List<String> inc = Splitter.on("=").limit(2).omitEmptyStrings().trimResults().splitToList(segments.get(i));
            if (inc.size() != 2) {
                throw new IllegalArgumentException("Malformed embed dependency clause: " + segments.get(i));
            }
            String attribute = inc.get(0);
            String value = inc.get(1);

            if ("scope".equalsIgnoreCase(attribute)) {
                final List<String> values = getMultiValues(value);
                filter.add(new ArtifactFilter() {
                    @Override
                    public boolean include(Artifact artifact) {
                        return values.contains(artifact.getScope()); //NOSONAR
                    }
                });
            }

            if ("exclude".equalsIgnoreCase(attribute)) {
                filter.add(new PatternExcludesArtifactFilter(getMultiValues(value)));
            }

            if ("type".equalsIgnoreCase(attribute)) {
                final List<String> values = getMultiValues(value);
                filter.add(new ArtifactFilter() {
                    @Override
                    public boolean include(Artifact artifact) {
                        return values.contains(artifact.getType()); //NOSONAR
                    }
                });
            }

            if ("optional".equalsIgnoreCase(attribute)) {
                final boolean v = Boolean.parseBoolean(value);
                filter.add(new ArtifactFilter() {
                    @Override
                    public boolean include(Artifact artifact) {
                        return artifact.isOptional() == v;
                    }
                });
            }

            if ("transitive".equalsIgnoreCase(attribute)) {
                final boolean v = Boolean.parseBoolean(value);
                configuration.setTransitive(v);
            }

            if ("inline".equalsIgnoreCase(attribute)) {
                configuration.setInline(value);
            }
        }

        return configuration;
    }

    private List<String> getMultiValues(String value) {
        return Splitter.on('|').trimResults().omitEmptyStrings().splitToList(value);
    }

    /**
     * Generates the new set of instruction containing the original set of instructions enhanced with the embed
     * dependencies results.
     *
     * @param instructions the current set of instructions
     * @param dependencies the project's dependencies
     * @return the final set of instructions
     */
    public Properties generate(Properties instructions, ProjectDependencies dependencies) {
        Properties result = new Properties();
        result.putAll(instructions);
        StringBuilder include = new StringBuilder();
        if (instructions.getProperty(Constants.INCLUDE_RESOURCE) != null) {
            include.append(instructions.getProperty(Constants.INCLUDE_RESOURCE));
        }
        StringBuilder classpath = new StringBuilder();
        final String originalClassPath = instructions.getProperty(Constants.BUNDLE_CLASSPATH);
        if (originalClassPath != null) {
            classpath.append(originalClassPath);
        }

        for (EmbeddedDependency configuration : embedded) {
            configuration.addClause(dependencies, include, classpath, reporter);
        }

        if (include.length() != 0) {
            result.setProperty(Constants.INCLUDE_RESOURCE, include.toString());
        }
        if (classpath.length() != 0) {
            if (originalClassPath != null) {
                result.setProperty(Constants.BUNDLE_CLASSPATH, classpath.toString());
            } else {
                // Prepend . to the classpath.
                result.setProperty(Constants.BUNDLE_CLASSPATH, "., " + classpath.toString());
            }
        }

        return result;

    }

    private class EmbeddedDependency {

        private final String clause;
        private ArtifactFilter filter;
        private String inline;
        private boolean full = true;
        private boolean transitive = embedTransitive;

        /**
         * Creates an instance of {@link org.wisdom.maven.osgi.DependencyEmbedder.EmbeddedDependency}.
         *
         * @param clause the clause
         */
        public EmbeddedDependency(String clause) {
            this.clause = clause;
        }

        /**
         * Sets the artifact filter.
         *
         * @param filter the filter
         */
        public void setFilter(ArtifactFilter filter) {
            this.filter = filter;
        }

        /**
         * Sets the inline attribute.
         *
         * @param inline the inline
         */
        public void setInline(String inline) {
            if ("true".equalsIgnoreCase(inline)) {
                this.full = true;
                return;
            }
            if ("false".equalsIgnoreCase(inline)) {
                this.full = false;
                return;
            }
            this.full = false;
            this.inline = inline;
        }

        private boolean inline() {
            return full || inline != null;
        }

        /**
         * Computes the include-resource and bundle-classpath clauses.
         *
         * @param dependencies the project's dependencies
         * @param include      the current include-resource
         * @param classpath    the current bundle-classpath
         * @param reporter     the reporter
         */
        public void addClause(ProjectDependencies dependencies, StringBuilder include,
                              StringBuilder classpath, Reporter reporter) {
            boolean generated = false;

            Set<Artifact> artifacts = dependencies.getDirectDependencies();
            if (transitive) {
                artifacts = dependencies.getTransitiveDependencies();
            }

            for (Artifact artifact : artifacts) {
                if (filter.include(artifact)) {
                    if (inline()) {
                        String clause = "@" + artifact.getFile().getAbsolutePath();
                        if (!full) {
                            clause += "!/" + inline;  //NOSONAR can be appended here.
                        }
                        append(include, clause);
                        generated = true;
                    } else {
                        append(include, artifact.getFile().getAbsolutePath());
                        append(classpath, artifact.getFile().getName());
                        generated = true;
                    }
                }
            }
            if (!generated) {
                reporter.warn("A clause from `Embed-Dependency` did not match any artifacts: " + clause);
            }
        }

        /**
         * Sets the transitive attribute.
         *
         * @param transitive whether or not the clause must analyze the transitive set of dependencies
         */
        public void setTransitive(boolean transitive) {
            this.transitive = transitive;
        }
    }

    private void append(StringBuilder builder, String content) {
        if (builder.length() != 0) {
            builder.append(", ");
        }
        builder.append(content);
    }
}

