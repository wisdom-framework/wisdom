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
