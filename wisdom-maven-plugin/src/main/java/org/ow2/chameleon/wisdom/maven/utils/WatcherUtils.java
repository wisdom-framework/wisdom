package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * A set of utility functions to ease watcher development.
 */
public class WatcherUtils {

    public static boolean isInDirectory(File file, File directory) {
        try {
            return FilenameUtils.directoryContains(directory.getCanonicalPath(), file.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    public static File getExternalAssetsSource(File baseDir) {
        return new File(baseDir, "src/main/assets");
    }

    public static File getExternalAssetsDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/assets");
    }

    public static File getConfigurationSource(File baseDir) {
        return new File(baseDir, "src/main/configuration");
    }

    public static File getConfigurationDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/configuration");
    }

    public static File getExternalTemplateSource(File baseDir) {
        return new File(baseDir, "src/main/templates");
    }

    public static File getExternalTemplateDestination(File baseDir) {
        return new File(baseDir, "target/wisdom/templates");
    }

    public static File getJavaSource(File baseDir) {
        return new File(baseDir, "src/main/java");
    }

    public static File getJavaDestination(File baseDir) {
        return new File(baseDir, "target/classes");
    }

    public static File getInternalAssetsSource(File baseDir) {
        return new File(baseDir, "src/main/assets");
    }

    public static File getInternalAssetsDestination(File baseDir) {
        return new File(baseDir, "target/classes/assets");
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

    public static File getResources(File baseDir) {
        return new File(baseDir, "src/main/resources");
    }
}
