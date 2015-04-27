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
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.wisdom.maven.Constants;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Resource copy utilities.
 */
public final class ResourceCopy {

    private ResourceCopy() {
        // Avoid direct instantiation.
    }

    /**
     * The list of extension is initializer in the static initializer of the class.
     */
    static final Set<String> NON_FILTERED_EXTENSIONS;

    static {
        NON_FILTERED_EXTENSIONS = new HashSet<>(Arrays.asList(
                "pdf",
                // Images
                "png",
                "bmp",
                "jpeg",
                "jpg",
                "tiff",
                // Archives
                "jar",
                "war",
                "zip",
                "tar.gz",
                "tgz",
                "gz",
                "tar",
                "nar",
                "rar",
                "bz2",
                // Flash and document
                "swf",
                "ogg",
                "mp3",
                "mpeg",
                "key",
                "doc",
                "docx",
                "ppt",
                "pptx",
                "dot",
                "pps",
                "xls",
                "xlsx",
                "flv",
                // Fonts
                "otf",
                "eot",
                "svg",
                "ttf",
                "woff",
                // Video formats
                "mpg",
                "qt",
                "mkv",
                "avi",
                "mp4",
                "ogv",
                "webm",
                // 3D format
                "dae",
                "stl",
                // Java Security
                "jks"));
    }

    /**
     * Let you globally add extensions to the non-filtered extension list.
     *
     * @param ext the extensions to add, does not add them if already in the list.
     */
    public static void addNonFilteredExtension(String... ext) {
        Collections.addAll(NON_FILTERED_EXTENSIONS, ext);
    }

    /**
     * Copies the file <tt>file</tt> to the directory <tt>dir</tt>, keeping the structure relative to <tt>rel</tt>.
     *
     * @param file                 the file to copy
     * @param rel                  the base 'relative'
     * @param dir                  the directory
     * @param mojo                 the mojo
     * @param filtering            the filtering component
     * @param additionalProperties additional properties
     * @throws IOException if the file cannot be copied.
     */
    public static void copyFileToDir(File file, File rel, File dir, AbstractWisdomMojo mojo, MavenResourcesFiltering
            filtering, Properties additionalProperties) throws
            IOException {
        if (filtering == null) {
            File out = computeRelativeFile(file, rel, dir);
            if (out.getParentFile() != null) {
                mojo.getLog().debug("Creating " + out.getParentFile() + " : " + out.getParentFile().mkdirs());
                FileUtils.copyFileToDirectory(file, out.getParentFile());
            } else {
                throw new IOException("Cannot copy file - parent directory not accessible for "
                        + file.getAbsolutePath());
            }
        } else {
            Resource resource = new Resource();
            resource.setDirectory(rel.getAbsolutePath());
            resource.setFiltering(true);
            resource.setTargetPath(dir.getAbsolutePath());
            resource.setIncludes(ImmutableList.of("**/" + file.getName()));

            List<String> excludedExtensions = new ArrayList<>();
            excludedExtensions.addAll(filtering.getDefaultNonFilteredFileExtensions());
            excludedExtensions.addAll(NON_FILTERED_EXTENSIONS);

            MavenResourcesExecution exec = new MavenResourcesExecution(ImmutableList.of(resource), dir, mojo.project,
                    "UTF-8", Collections.<String>emptyList(), excludedExtensions, mojo.session);

            if (additionalProperties != null) {
                exec.setAdditionalProperties(additionalProperties);
            }
            exec.setEscapeString("\\");

            try {
                filtering.filterResources(exec);
            } catch (MavenFilteringException e) {
                throw new IOException("Error while copying resources", e);
            }
        }
    }

    /**
     * Gets a File object representing a File in the directory <tt>dir</tt> which has the same path as the file
     * <tt>file</tt> from the directory <tt>rel</tt>.
     * <p>
     * For example, copying root/foo/bar.txt to out with rel set to root returns the file out/foo/bar.txt.
     *
     * @param file the file to copy
     * @param rel  the base 'relative'
     * @param dir  the output directory
     * @return the destination file
     */
    public static File computeRelativeFile(File file, File rel, File dir) {
        String path = file.getAbsolutePath();
        String relativePath = path.substring(rel.getAbsolutePath().length());
        return new File(dir, relativePath);
    }

    /**
     * Copies the configuration from "src/main/configuration" to "wisdom/conf". Copied resources are filtered.
     *
     * @param mojo      the mojo
     * @param filtering the component required to filter resources
     * @throws IOException if a file cannot be copied
     */
    public static void copyConfiguration(AbstractWisdomWatcherMojo mojo, MavenResourcesFiltering filtering) throws
            IOException {
        File in = new File(mojo.basedir, Constants.CONFIGURATION_SRC_DIR);
        if (in.isDirectory()) {
            File out = new File(mojo.getWisdomRootDirectory(), Constants.CONFIGURATION_DIR);
            filterAndCopy(mojo, filtering, in, out);
        } else {
            mojo.getLog().warn("No configuration directory (src/main/configuration) - use this mode at your own risk");
            mojo.getLog().warn("A fake application configuration is going to be created, " +
                    "using a fake application secret, do not use this file in production");
            // No configuration directory, generate a fake application configuration
            File conf = new File(mojo.getWisdomRootDirectory(), "conf");
            mojo.getLog().debug("Creating conf directory " + conf.mkdirs());
            File output = new File(conf, "application.conf");
            ApplicationSecretGenerator.generateFakeConfiguration(output);
        }
    }

    /**
     * Copies the external assets from "src/main/assets" to "wisdom/assets". Copied resources are filtered.
     *
     * @param mojo      the mojo
     * @param filtering the component required to filter resources
     * @throws IOException if a file cannot be copied
     */
    public static void copyExternalAssets(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering) throws IOException {
        File in = new File(mojo.basedir, Constants.ASSETS_SRC_DIR);
        if (!in.exists()) {
            return;
        }
        File out = new File(mojo.getWisdomRootDirectory(), Constants.ASSETS_DIR);
        filterAndCopy(mojo, filtering, in, out);
    }

    /**
     * Copies the external templates from "src/main/templates" to "wisdom/templates". Copied resources are filtered.
     *
     * @param mojo      the mojo
     * @param filtering the component required to filter resources
     * @throws IOException if a file cannot be copied
     */
    public static void copyTemplates(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering) throws IOException {
        File in = new File(mojo.basedir, Constants.TEMPLATES_SRC_DIR);
        if (!in.exists()) {
            return;
        }
        File out = new File(mojo.getWisdomRootDirectory(), Constants.TEMPLATES_DIR);

        filterAndCopy(mojo, filtering, in, out);
    }

    /**
     * Copies the internal resources from "src/main/resources" to "target/classes". Copied resources are filtered.
     * Notice that these resources are embedded in the application's bundle.
     *
     * @param mojo      the mojo
     * @param filtering the component required to filter resources
     * @throws IOException if a file cannot be copied
     */
    public static void copyInternalResources(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering) throws
            IOException {
        File in = new File(mojo.basedir, Constants.MAIN_RESOURCES_DIR);
        if (!in.exists()) {
            return;
        }
        File out = new File(mojo.buildDirectory, "classes");
        filterAndCopy(mojo, filtering, in, out);
    }

    private static void filterAndCopy(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering, File in, File out) throws IOException {
        Resource resource = new Resource();
        resource.setDirectory(in.getAbsolutePath());
        resource.setFiltering(true);
        resource.setTargetPath(out.getAbsolutePath());

        List<String> excludedExtensions = new ArrayList<>();
        excludedExtensions.addAll(filtering.getDefaultNonFilteredFileExtensions());
        excludedExtensions.addAll(NON_FILTERED_EXTENSIONS);

        MavenResourcesExecution exec = new MavenResourcesExecution(ImmutableList.of(resource), out, mojo.project,
                "UTF-8", Collections.<String>emptyList(), excludedExtensions, mojo.session);
        exec.setEscapeString("\\");

        try {
            filtering.filterResources(exec);
        } catch (MavenFilteringException e) {
            throw new IOException("Error while copying resources", e);
        }
    }

    /**
     * Copies the `cfg` files from `src/main/instances` to the Wisdom application directory.
     *
     * @param mojo      the mojo
     * @param filtering the filtering support
     * @throws IOException if file cannot be copied
     */
    public static void copyInstances(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering) throws IOException {
        File in = new File(mojo.basedir, Constants.INSTANCES_SRC_DIR);
        if (in.isDirectory()) {
            File out = new File(mojo.getWisdomRootDirectory(), Constants.APPLICATION_DIR);
            filterAndCopy(mojo, filtering, in, out);
        }
    }
}
