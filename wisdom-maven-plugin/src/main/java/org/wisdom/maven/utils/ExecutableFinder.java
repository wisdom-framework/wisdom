package org.wisdom.maven.utils;

import java.io.File;

/**
 * Try to find an executable.
 */
public class ExecutableFinder {

    public static final String[] EXECUTABLE_EXTENSIONS = new String[] {
            "", ".sh", ".exe", ".bat"
    };

    private ExecutableFinder() {
        // Avoid instantiation.
    }

    public static  File find(String executable, File... hints) {
        // First try using the hints.
        if (hints != null) {
            for (File hint : hints) {
                File file = attemptToFindExecutableInDirectory(hint, executable);
                if (file != null) {
                    return file;
                }
            }
        }

        // Not found, try to use the system path.
        return findExecutableInSystemPath(executable);
    }

    public static File attemptToFindExecutableInDirectory(File directory, String executable) {
        for (String extension : EXECUTABLE_EXTENSIONS) {
            File file = new File(directory, executable + extension);
            if (file.isFile()  && file.canExecute()) {
                return file;
            }
        }
        return null;
    }

    public static File findExecutableInSystemPath(String executable) {
        String systemPath = System.getenv("PATH");

        // Fast failure if we don't have the PATH defined.
        if (systemPath == null) {
            return null;
        }

        String[] pathDirs = systemPath.split(File.pathSeparator);

        for (String pathDir : pathDirs) {
            File dir = new File(pathDir);
            if (dir.isDirectory()) {
                File file = attemptToFindExecutableInDirectory(dir, executable);
                if (file != null) {
                    return file;
                }
            }
        }
        // :-(
        return null;
    }

}
