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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.slf4j.LoggerFactory;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;

/**
 * Utility methods to handle webjars.
 */
public class WebJars {
    /**
     * A regex extracting the library name and version from Zip Entry names.
     */
    public static final Pattern WEBJAR_REGEX = Pattern.compile(".*META-INF/resources/webjars/([^/]+)/([^/]+)/.*");
    /**
     * The directory within jar file where webjar resources are located.
     */
    public static final String WEBJAR_LOCATION = "META-INF/resources/webjars/";

    /**
     * A regex to extract the different part of the path of a file from a library included in a webjar.
     */
    public static final Pattern WEBJAR_INTERNAL_PATH_REGEX = Pattern.compile("([^/]+)/([^/]+)/(.*)");

    /**
     * Manage webjars dependencies.
     * <p>
     * This process is executed as follows:
     * <ol>
     * <li>web jars that are also bundles are ignored</li>
     * <li>web jars libraries from a 'provided' dependency (in the 'provided' scope) are copied to the /assets/lib
     * directory.</li>
     * <li>web jars libraries from a 'compile' dependency (in the 'compile' scope) are copied to the application
     * directory.</li>
     * <li>Transitive are also analyzed if enabled (enabled by default).</li>
     * </ol>
     *
     * @param mojo       the mojo
     * @param graph      the dependency graph builder
     * @param transitive whether or not we include the transitive dependencies.
     * @param unpackWebJars whether or not webjars should be extracted to target/webjars
     * @throws java.io.IOException when a web jar cannot be handled correctly
     */
    public static void manageWebJars(AbstractWisdomMojo mojo, DependencyGraphBuilder graph,
                                     boolean transitive, boolean unpackWebJars) throws IOException {
        File webjars = new File(mojo.getWisdomRootDirectory(), "assets/libs");
        final File application = new File(mojo.getWisdomRootDirectory(), "application");

        Set<Artifact> artifacts = DependencyCopy.getArtifactsToConsider(mojo, graph, transitive, null);


        for (Artifact artifact : artifacts) {
            if (DependencyCopy.SCOPE_COMPILE.equalsIgnoreCase(artifact.getScope())
                    || DependencyCopy.SCOPE_PROVIDED.equalsIgnoreCase(artifact.getScope())) {
                File file = artifact.getFile();

                // Check it's a 'jar file'
                if (!file.getName().endsWith(".jar")) {
                    mojo.getLog().debug("Dependency " + file.getName() + " is not a web jar, it's not even a jar file");
                    continue;
                }

                // Check that it's a web jar.
                if (!isWebJar(file)) {
                    mojo.getLog().debug("Dependency " + file.getName() + " is not a web jar.");
                    continue;
                }

                // Check that it's not a bundle.
                if (isBundle(file)) {
                    mojo.getLog().debug("Dependency " + file.getName() + " is a web jar, but it's also a bundle, " +
                            "to ignore it.");
                    continue;
                }

                // It's a web jar.
                if (DependencyCopy.SCOPE_COMPILE.equalsIgnoreCase(artifact.getScope())) {
                    mojo.getLog().info("Copying web jar library " + file.getName() + " to the application directory");
                    FileUtils.copyFileToDirectory(file, application);

                    // Check whether or not it must be unpacked to target/webjars.
                    if (unpackWebJars) {
                        extract(mojo, file, new File(mojo.buildDirectory, "webjars"), true);
                    }
                    // NOTE: webjars from the 'provided' scope are not unpacked in target/webjars as they are in
                    // target/wisdom/assets/libs.
                } else {
                    mojo.getLog().info("Extracting web jar libraries from " + file.getName() + " to " + webjars
                            .getAbsolutePath());
                    extract(mojo, file, webjars, false);
                }
            }
        }
    }

    /**
     * Checks whether the given file is a WebJar or not (http://www.webjars.org/documentation).
     * The check is based on the presence of {@literal META-INF/resources/webjars/} directory in the jar file.
     *
     * @param file the file.
     * @return {@literal true} if it's a bundle, {@literal false} otherwise.
     */
    public static boolean isWebJar(File file) {
        Set<String> found = new LinkedHashSet<>();
        if (file.isFile() && file.getName().endsWith(".jar")) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);

                // Fast return if the base structure is not there
                if (jar.getEntry(WEBJAR_LOCATION) == null) {
                    return false;
                }

                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    Matcher matcher = WEBJAR_REGEX.matcher(entry.getName());
                    if (matcher.matches()) {
                        found.add(matcher.group(1) + "-" + matcher.group(2));
                    }
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(DependencyCopy.class).error("Cannot check if the file {} is a webjar, " +
                        "cannot open it", file.getName(), e);
                return false;
            } finally {
                final JarFile finalJar = jar;
                IOUtils.closeQuietly(new Closeable() {
                    @Override
                    public void close() throws IOException {
                        if (finalJar != null) {
                            finalJar.close();
                        }
                    }
                });
            }

            for (String lib : found) {
                LoggerFactory.getLogger(DependencyCopy.class).info("Web Library found in {} : {}",
                        file.getName(), lib);
            }

            return !found.isEmpty();
        }

        return false;
    }

    private static void extract(final AbstractWisdomMojo mojo, File in, File out, boolean stripVersion) throws IOException {
        ZipFile file = new ZipFile(in);
        try {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith(WEBJAR_LOCATION) && !entry.isDirectory()) {
                    // Compute destination.
                    File output = new File(out,
                            entry.getName().substring(WEBJAR_LOCATION.length()));
                    if (stripVersion) {
                        String path = entry.getName().substring(WEBJAR_LOCATION.length());
                        Matcher matcher = WEBJAR_INTERNAL_PATH_REGEX.matcher(path);
                        if (matcher.matches()) {
                            output = new File(out, matcher.group(1) + "/" + matcher.group(3));
                        } else {
                            mojo.getLog().warn(path + " does not match the regex - did not strip the version for this" +
                                    " file");
                        }
                    }
                    InputStream stream = null;
                    try {
                        stream = file.getInputStream(entry);
                        output.getParentFile().mkdirs();
                        FileUtils.copyInputStreamToFile(stream, output);
                    } catch (IOException e) {
                        mojo.getLog().error("Cannot unpack " + entry.getName() + " from " + file.getName(), e);
                        throw e;
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(file);
        }
    }
}
