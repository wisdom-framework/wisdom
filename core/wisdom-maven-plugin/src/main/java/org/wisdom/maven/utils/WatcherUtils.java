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
import org.apache.commons.io.FilenameUtils;
import org.wisdom.maven.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A set of utility functions to ease watcher development.
 */
public class WatcherUtils implements Constants {

    /**
     * Checks whether the given file is inside the given directory.
     *
     * @param file      the file
     * @param directory the directory
     * @return {@literal true} if the file is in the directory (or any subdirectories), {@literal false} otherwise.
     */
    public static boolean isInDirectory(File file, File directory) {
        try {
            return FilenameUtils.directoryContains(directory.getCanonicalPath(), file.getCanonicalPath());
        } catch (IOException e) { //NOSONAR
            return false;
        }
    }

    /**
     * Gets the external asset directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/src/main/assets directory
     */
    public static File getExternalAssetsSource(File baseDir) {
        return new File(baseDir, ASSETS_SRC_DIR);
    }

    /**
     * Gets the external asset destination directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/target/wisdom/assets directory
     */
    public static File getExternalAssetsDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/" + ASSETS_DIR);
    }

    /**
     * Gets the configuration directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/src/main/configuration directory
     */
    public static File getConfigurationSource(File baseDir) {
        return new File(baseDir, CONFIGURATION_SRC_DIR);
    }

    /**
     * Gets the configuration destination directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/target/wisdom/conf directory
     */
    public static File getConfigurationDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/" + CONFIGURATION_DIR);
    }

    /**
     * Gets the external template directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/src/main/templates directory
     */
    public static File getExternalTemplateSource(File baseDir) {
        return new File(baseDir, TEMPLATES_SRC_DIR);
    }

    /**
     * Gets the external templates destination directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/target/wisdom/templates directory
     */
    public static File getExternalTemplateDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/" + TEMPLATES_DIR);
    }

    /**
     * Gets the Java source directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/src/main/java directory
     */
    public static File getJavaSource(File baseDir) {
        return new File(baseDir, MAIN_SRC_DIR);
    }

    /**
     * Gets the Java classes destination directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/target/classes directory
     */
    public static File getJavaDestination(File baseDir) {
        return new File(baseDir, "target/classes");
    }

    /**
     * Gets the internal asset directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/src/main/resources/assets directory
     */
    public static File getInternalAssetsSource(File baseDir) {
        return new File(baseDir, MAIN_RESOURCES_DIR + "/" + ASSETS_DIR);
    }

    /**
     * Gets the internal resource directory.
     *
     * @param baseDir the project's base dir.
     * @return the BASE/src/main/resources directory
     */
    public static File getResources(File baseDir) {
        return new File(baseDir, MAIN_RESOURCES_DIR);
    }

    /**
     * Checks whether the given file has one of the given extension.
     *
     * @param file       the file
     * @param extensions the extensions
     * @return {@literal true} if the file has one of the given extension, {@literal false} otherwise
     */
    public static boolean hasExtension(File file, String... extensions) {
        String extension = FilenameUtils.getExtension(file.getName());
        for (String s : extensions) {
            if (extension.equalsIgnoreCase(s) || ("." + extension).equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the given file has one of the given extension.
     *
     * @param file       the file
     * @param extensions the extensions
     * @return {@literal true} if the file has one of the given extension, {@literal false} otherwise
     */
    public static boolean hasExtension(File file, List<String> extensions) {
        String extension = FilenameUtils.getExtension(file.getName());
        for (String s : extensions) {
            if (extension.equalsIgnoreCase(s) || ("." + extension).equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all the file from the given directory having one of the specified extension. The lookup is recursive (so
     * searches in nested directories). If the given directory does not exist or is not a directory,
     * this method returns an empty collection.
     *
     * @param directory  the directory
     * @param extensions the set of extension
     * @return the set of file, potentially empty
     */
    public static Collection<File> getAllFilesFromDirectory(File directory, List<String> extensions) {
        if (directory.isDirectory()) {
            return FileUtils.listFiles(directory,
                    extensions.toArray(new String[extensions.size()]), true);
        }
        return Collections.emptyList();
    }
}
