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

import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A class responsible for collecting metadata on the current project.
 * This class has been done to increase the testability of the bundle packager, as you can mock the output of the
 * different methods.
 */
public class ProjectScanner {

    private final File basedir;

    /**
     * Creates the project scanner.
     *
     * @param basedir the project's base directory
     */
    public ProjectScanner(File basedir) {
        if (!basedir.isDirectory()) {
            throw new IllegalStateException("Cannot instantiate a scanner without an existing basedir");
        }

        this.basedir = basedir;
    }

    /**
     * @return the classes directory.
     */
    public File getClassesDirectory() {
        return new File(basedir, "target/classes");
    }

    /**
     * Gets the list of packages from {@literal src/main/java}. This method scans for ".class" files in {@literal
     * target/classes}.
     *
     * @return the list of packages, empty if none.
     */
    public Set<String> getPackagesFromMainSources() {
        Set<String> packages = new LinkedHashSet<>();
        File classes = getClassesDirectory();
        if (classes.isDirectory()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(classes);
            scanner.setIncludes(new String[]{"**/*.class"});
            scanner.addDefaultExcludes();
            scanner.scan();

            for (int i = 0; i < scanner.getIncludedFiles().length; i++) {
                packages.add(Packages.getPackageName(scanner.getIncludedFiles()[i]));
            }
        }

        return packages;
    }

    /**
     * Gets the list of packages from {@literal src/test/java}. This method scans for ".class" files in {@literal
     * target/test-classes}.
     *
     * @return the list of packages, empty if none.
     */
    public Set<String> getPackagesFromTestSources() {
        Set<String> packages = new LinkedHashSet<>();
        File classes = new File(basedir, "target/test-classes");
        if (classes.isDirectory()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(classes);
            scanner.setIncludes(new String[]{"**/*.class"});
            scanner.addDefaultExcludes();
            scanner.scan();

            for (int i = 0; i < scanner.getIncludedFiles().length; i++) {
                packages.add(Packages.getPackageName(scanner.getIncludedFiles()[i]));
            }
        }

        return packages;
    }

    /**
     * Gets the list of resource files from {@literal src/main/resources} or {@literal src/test/resources}.
     * This method scans for all files that are not classes from {@literal target/classes} or {@literal
     * target/test-classes}. The distinction is made according to the value of {@code test}.
     *
     * @param test whether or not we analyse tests resources.
     * @return the list of packages, empty if none.
     */
    public Set<String> getLocalResources(boolean test) {
        Set<String> resources = new LinkedHashSet<>();
        File classes = getClassesDirectory();
        if (test) {
            classes = new File(basedir, "target/test-classes");
        }
        if (classes.isDirectory()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(classes);
            scanner.setExcludes(new String[]{"**/*.class"});
            scanner.addDefaultExcludes();
            scanner.scan();

            Collections.addAll(resources, scanner.getIncludedFiles());
        }

        return resources;
    }

}
