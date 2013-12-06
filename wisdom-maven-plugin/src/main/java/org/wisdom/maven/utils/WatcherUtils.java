package org.wisdom.maven.utils;

import org.apache.commons.io.FilenameUtils;
import org.wisdom.maven.Constants;

import java.io.File;
import java.io.IOException;

/**
 * A set of utility functions to ease watcher development.
 */
public class WatcherUtils implements Constants {

    public static boolean isInDirectory(File file, File directory) {
        try {
            return FilenameUtils.directoryContains(directory.getCanonicalPath(), file.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    public static File getExternalAssetsSource(File baseDir) {
        return new File(baseDir, ASSETS_SRC_DIR);
    }

    public static File getExternalAssetsDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/" + ASSETS_DIR);
    }

    public static File getConfigurationSource(File baseDir) {
        return new File(baseDir, CONFIGURATION_SRC_DIR);
    }

    public static File getConfigurationDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/" + CONFIGURATION_DIR);
    }

    public static File getExternalTemplateSource(File baseDir) {
        return new File(baseDir, TEMPLATES_SRC_DIR);
    }

    public static File getExternalTemplateDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/" + TEMPLATES_DIR);
    }

    public static File getJavaSource(File baseDir) {
        return new File(baseDir, MAIN_SRC_DIR);
    }

    public static File getJavaDestination(File baseDir) {
        return new File(baseDir, "target/classes");
    }

    public static File getInternalAssetsSource(File baseDir) {
        return new File(baseDir, MAIN_RESOURCES_DIR + "/" + ASSETS_DIR);
    }

    public static File getInternalAssetsDestination(File baseDir) {
        return new File(baseDir, "target/classes/" + ASSETS_DIR);
    }

    public static File getResources(File baseDir) {
        return new File(baseDir, MAIN_RESOURCES_DIR);
    }

    public static boolean hasExtension(File file, String... extensions) {
        String extension = FilenameUtils.getExtension(file.getName());
        for (String s : extensions) {
            if (extension.equals(s)  || ("." + extension).equals(s)) {
                return true;
            }
        }
        return false;
    }
}
