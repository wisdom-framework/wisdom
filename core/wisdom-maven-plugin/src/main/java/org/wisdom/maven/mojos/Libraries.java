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

import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents libraries.
 * <p>
 * Libraries is a part of the wisdom-maven-plugin configuration and let you configure the dependencies that are not
 * bundle, but still available from Wisdom's applications. Technically, there are copied to the 'libs' directory of
 * the Wisdom server.
 * <p>
 * To be valid, the configuration must contain a set of {@literal included} dependencies. The dependencies are selected
 * using the ":" syntax such as {@literal :artifactid} or {@literal groupId:artifactId}. This means that the {@code
 * includes} element must be defined and contain {@code include} elements. If none
 * is present, no libraries will be copied. Artifact coordinates may be given in
 * simple groupId:artifactId form, or they may be fully qualified in the form
 * {@code groupId:artifactId:type[:classifier]:version}. Additionally, wildcards can be used, as in {@code *:wisdom-*}.
 * <p>
 * You can configure whether or not transitive dependencies need to be handled (copied to the 'libs' directory) to. In
 * this case, you can filter out dependencies using the {@literal excludes} list.
 * <p>
 * In addition, with {@literal excludeFromApplication} (set to {@code false} by default),
 * you can decide to exclude the dependencies from the 'application' directory if there are copied in the 'libs'
 * directory. In other word, if one of the dependency copied to 'libs' is an OSGi bundle,
 * it should also be copied to 'application'. With this settings, it won't be copied to 'application'.
 * <p>
 * Notice that using libraries does not embrace the modularity and dynamism model of Wisdom. First,
 * the set of libraries is defined when Wisdom starts and can't changed at runtime. In addition,
 * libraries required their dependencies to also be a library.
 */
public class Libraries {

    private List<String> includes = new ArrayList<>();

    private List<String> excludes = new ArrayList<>();

    private boolean excludeFromApplication = false;

    private boolean resolveTransitive = true;

    /**
     * @return the set of selector defining the set of dependencies considered as library. Each selector is defined
     * using the ":" Maven syntax as {@code groupId:artifactId:type[:classifier]:version}.
     */
    public List<String> getIncludes() {
        return includes;
    }

    /**
     * When {@code includes} subelements are present in the libraries, they define a set of artifact coordinates to
     * include as libraries. If none is present, no libraries will be copied. Artifact coordinates may be given in
     * simple groupId:artifactId form, or they may be fully qualified in the form
     * {@code groupId:artifactId:type[:classifier]:version}. Additionally, wildcards can be used,
     * as in {@code *:wisdom-*}
     *
     * @param includes the set of includes.
     */
    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    /**
     * @return the set of dependencies that must not be considered as a dependency. The {@code
     * groupId:artifactId:type[:classifier]:version} syntax is used to select those dependencies. This attribute is
     * only useful when {@link #resolveTransitive} is set to {@code true}.
     */
    public List<String> getExcludes() {
        return excludes;
    }

    /**
     * Sets the dependencies that must not be considered as a dependency. The {@code
     * groupId:artifactId:type[:classifier]:version} syntax is used to select those dependencies. This attribute is
     * only useful when {@link #resolveTransitive} is set to {@code true}.
     *
     * @param excludes the set of exclusion.
     */
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * Checks whether we should exclude the dependencies considered as 'libs' from the set of dependency copies to
     * the 'application' directory.
     *
     * @return {@code true} if we must exclude the dependencies or not.
     */
    public boolean isExcludeFromApplication() {
        return excludeFromApplication;
    }

    /**
     * Sets whether or not we should ignore the dependencies that are eligible bundles if they are considered as
     * libraries. This means that if a dependency is a bundle (so should be copied to the 'application' directory),
     * but is selected as a library, it will only be copied to the 'libs' directory.
     *
     * @param excludeFromApplication the flag value.
     */
    public void setExcludeFromApplication(boolean excludeFromApplication) {
        this.excludeFromApplication = excludeFromApplication;
    }

    /**
     * Checks whether the library resolution must consider transitive dependencies. If enabled,
     * all 'compile' dependencies from a selected libraries are also copied to the 'libs' directory.
     *
     * @return {@code true} if the transitive support is enabled, {@code false} otherwise.
     */
    public boolean isResolveTransitive() {
        return resolveTransitive;
    }

    /**
     * Enables or disables the transitive dependency resolution when analysing selected libraries.  If enabled,
     * all 'compile' dependencies from a selected libraries are also copied to the 'libs' directory.
     *
     * @param resolveTransitive the flag
     */
    public void setResolveTransitive(boolean resolveTransitive) {
        this.resolveTransitive = resolveTransitive;
    }

    /**
     * @return Whether this libraries object have 'includes'. Without includes, no libraries are copied to 'libs'.
     */
    public boolean hasLibraries() {
        return !includes.isEmpty();
    }

    /**
     * @return the selection filter allowing to select the set of libraries.
     */
    public ArtifactFilter getFilter() {
        AndArtifactFilter filter = new AndArtifactFilter();
        PatternIncludesArtifactFilter incl = new PatternIncludesArtifactFilter(getIncludes(), isResolveTransitive());
        filter.add(incl);
        if (!getExcludes().isEmpty()) {
            PatternExcludesArtifactFilter excl = new PatternExcludesArtifactFilter(getExcludes(),
                    isResolveTransitive());
            filter.add(excl);
        }
        return filter;
    }

    /**
     * @return a selection filter that reverse the {@link #getFilter()} selection. This filter is used to determine
     * whether a dependency must be ignored during  the bundle copy because of the {@link #excludeFromApplication}
     * parameter.
     */
    public ArtifactFilter getReverseFilter() {
        if (!hasLibraries()) {
            return null;
        }
        AndArtifactFilter filter = new AndArtifactFilter();
        PatternExcludesArtifactFilter excl = new PatternExcludesArtifactFilter(getIncludes(), isResolveTransitive());
        filter.add(excl);
        if (!getExcludes().isEmpty()) {
            PatternIncludesArtifactFilter incl = new PatternIncludesArtifactFilter(getExcludes(),
                    isResolveTransitive());
            filter.add(incl);
        }
        return filter;
    }

}
