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
package org.wisdom.resources;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.ExtensionBasedDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;

/**
 * Tracks the webjars from the 'hot' directories (managed by Chameleon) and manage them.
 */
public class WebJarDeployer extends ExtensionBasedDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebJarDeployer.class);

    private final BundleContext context;

    private final File cache;
    private final WebJarController controller;

    private Set<FileWebJarLib> libs = new LinkedHashSet<>();
    private ServiceRegistration<Deployer> reg;

    /**
     * Creates an install of the {@link org.wisdom.resources.WebJarDeployer}.
     *
     * @param context          the bundle context
     * @param webJarController the instance of controller in which libraries are added and removed
     */
    public WebJarDeployer(BundleContext context, WebJarController webJarController) {
        super("jar");
        this.context = context;
        this.controller = webJarController;
        cache = context.getBundle().getDataFile("webjars");
        if (!cache.isDirectory()) {
            boolean made = cache.mkdirs();
            LOGGER.debug("Creating webjars directory : {}", made);
        }
    }

    /**
     * A new file was created. It checks whether or not this file contained web jar libraries,
     * and if so proceed to their installation (i.e. un-packaking and indexation).
     *
     * @param file the file
     */
    @Override
    public synchronized void onFileCreate(File file) {
        final Set<DetectedWebJar> listOfDetectedWebJarLib = isWebJar(file);
        if (listOfDetectedWebJarLib != null) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);
                List<FileWebJarLib> installed = new ArrayList<>();
                for (DetectedWebJar detected : listOfDetectedWebJarLib) {
                    FileWebJarLib lib = expand(detected, jar);
                    if (lib != null) {
                        libs.add(lib);
                        installed.add(lib);
                        LOGGER.info("{} unpacked to {}", lib.name, lib.root.getAbsolutePath());
                    }
                }
                controller.addWebJarLibs(installed);
            } catch (IOException e) {
                LOGGER.error("Cannot open the jar file {}", file.getAbsolutePath(), e);
            } finally {
                IOUtils.closeQuietly(jar);
            }

        }
    }

    private FileWebJarLib expand(DetectedWebJar lib, JarFile jar) {
        File out = new File(cache, lib.id);
        Enumeration<? extends ZipEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith(WebJarController.WEBJAR_LOCATION) && !entry.isDirectory()) {
                // Compute destination.
                File output = new File(out,
                        entry.getName().substring(WebJarController.WEBJAR_LOCATION.length()));
                InputStream stream = null;
                try {
                    stream = jar.getInputStream(entry);
                    boolean made = output.getParentFile().mkdirs();
                    LOGGER.debug("{} directory created : {} ",
                            output.getParentFile().getAbsolutePath(), made);
                    FileUtils.copyInputStreamToFile(stream, output);
                } catch (IOException e) {
                    LOGGER.error("Cannot unpack " + entry.getName() + " from " + lib.file.getName(), e);
                    return null;
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        }
        File root = new File(out, lib.name + "/" + lib.version);
        return new FileWebJarLib(lib.name, lib.version, root, lib.file.getName());
    }

    /**
     * An accepted file was updated.
     * This methods acts as a file removal and creation.
     *
     * @param file the file
     */
    @Override
    public synchronized void onFileChange(File file) {
        onFileDelete(file);
        onFileCreate(file);
    }

    /**
     * An accepted file was deleted. We remove the contained libraries from the list and delete the directory in
     * which the library was contained.
     * <p>
     * We can't open it to find the contained web jars,
     * to we need to use the 'source' we set when the {@link org.wisdom.resources.FileWebJarLib} instances were created.
     *
     * @param file the file
     */
    @Override
    public synchronized void onFileDelete(File file) {
        // The file is already deleted, so we can't open it.
        // So we use the source.
        Set<FileWebJarLib> copy = new LinkedHashSet<>(libs);
        List<FileWebJarLib> toRemove = new ArrayList<>();
        for (FileWebJarLib lib : copy) {
            if (lib.source != null && lib.source.equals(file.getName())) {
                // Found, remove it.
                libs.remove(lib);
                toRemove.add(lib);
                // Delete the directory
                FileUtils.deleteQuietly(lib.root);
            }
        }

        controller.removeWebJarLibs(toRemove);
    }

    /**
     * Checks whether the given file is a WebJar or not (http://www.webjars.org/documentation).
     * The check is based on the presence of {@literal META-INF/resources/webjars/} directory in the jar file.
     *
     * @param file the file.
     * @return the set of libraries found in the file, {@code null} if none.
     */
    public static Set<DetectedWebJar> isWebJar(File file) {
        Set<DetectedWebJar> found = new LinkedHashSet<>();
        if (file.isFile() && file.getName().endsWith(".jar")) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);

                // Fast return if the base structure is not there
                if (jar.getEntry(WebJarController.WEBJAR_LOCATION) == null) {
                    return null;
                }

                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    Matcher matcher = WebJarController.WEBJAR_REGEX.matcher(entry.getName());
                    if (matcher.matches()) {
                        found.add(new DetectedWebJar(matcher.group(1), matcher.group(2), entry.getName(), file));
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Cannot check if the file {} is a webjar, " +
                        "cannot open it", file.getName(), e);
                return null;
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

            for (DetectedWebJar lib : found) {
                LOGGER.info("Web Library found in {} : {}",
                        file.getName(), lib.id);
            }

            return found;
        }

        return null;
    }

    /**
     * Registers the deployer service.
     */
    public synchronized void start() {
        reg = context.registerService(Deployer.class, this, null);
    }

    /**
     * Un-registers the deployer service.
     */
    public synchronized void stop() {
        if (reg != null) {
            reg.unregister();
        }
    }

    /**
     * A structure holding a webjar found in a jar file.
     * Don't forget that a single jar file can contain any number of libraries.
     */
    private static final class DetectedWebJar {

        /**
         * The id computed as follows: {@code name-version}.
         */
        public final String id;
        /**
         * the name of the library.
         */
        public final String name;
        /**
         * the version of the library.
         */
        public final String version;
        /**
         * the path within the jar file.
         */
        public final String path;
        /**
         * the file containing the library.
         */
        public final File file;

        /**
         * Creates an instance of {@link org.wisdom.resources.WebJarDeployer.DetectedWebJar}.
         *
         * @param name    the name
         * @param version the version
         * @param path    the path
         * @param file    the file
         */
        private DetectedWebJar(String name, String version, String path, File file) {
            this.name = name;
            this.version = version;
            this.path = path;
            this.id = name + "-" + version;
            this.file = file;
        }

        /**
         * Two {@link org.wisdom.resources.WebJarDeployer.DetectedWebJar} instances are equal if they have the same id.
         *
         * @param obj the object to compare with.
         * @return {@code true} if the two compared objects have the same id, {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            // We don't use instanceOf because we don't need inheritance (the class if final).
            return obj != null // Check not null
                    && obj.getClass().equals(DetectedWebJar.class) // Check it's the right class
                    && id.equals(((DetectedWebJar) obj).id); // Check id
        }

        /**
         * Computes the hash code of the current instance.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + version.hashCode();
            return result;
        }
    }
}
