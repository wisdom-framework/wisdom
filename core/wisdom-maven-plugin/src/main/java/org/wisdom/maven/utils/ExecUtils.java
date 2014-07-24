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
import java.util.ArrayList;
import java.util.List;

/**
 * Some helper methods related to command execution.
 * TODO Merge with Executable Finder.
 */
public class ExecUtils {

    public static File findExecutableInPath(String exec) {
        // Build candidates
        List<String> candidates = new ArrayList<String>();
        candidates.add(exec);
        // Windows:
        candidates.add(exec + ".exe");
        candidates.add(exec + ".bat");
        candidates.add(exec + ".cmd");
        // Linux / Unix / MacOsX
        candidates.add(exec + ".sh");
        candidates.add(exec + ".bash");

        String systemPath = System.getenv("PATH");

        // Fast failure if we don't have the PATH defined.
        if (systemPath == null) {
            return null;
        }

        String[] pathDirs = systemPath.split(File.pathSeparator);

        for (String pathDir : pathDirs) {
            for (String candidate : candidates) {
                File file = new File(pathDir, candidate);
                if (file.isFile()) {
                    return file;
                }
            }
        }

        // Search not successful.
        return null;
    }

    /**
     * Checks whether the current operating system is Windows.
     * This check use the <tt>os.name</tt> system property.
     *
     * @return <code>true</code> if the os is windows, <code>false</code> otherwise.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.contains("nix") || os.contains("nux")  || os.contains("aix");
    }

    public static boolean is64bit() {
        String arch = System.getProperty("sun.arch.data.model");
        if (arch == null) {
            arch = System.getProperty("os.arch");
        }
        return arch != null && arch.contains("64");
    }

    public static boolean isARM(){
        final String arch = System.getProperty("os.arch");
        return arch != null && arch.contains("arm");
    }
}
