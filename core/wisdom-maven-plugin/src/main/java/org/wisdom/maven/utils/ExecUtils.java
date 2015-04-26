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

import com.google.common.base.Strings;

import java.io.File;

/**
 * Some helper methods related to program execution.
 * <p>
 * <strong>Note:</strong> the method `is64bit` has been replaced by `is64bits` in the version 0.7.
 */
public class ExecUtils {

    /**
     * The set of extensions (including the empty extension) to append to the searched executable name.
     */
    private static final String[] EXECUTABLE_EXTENSIONS = new String[]{
            // The command itself (no extension)
            "",
            // Linux and Unix (scripts)
            ".sh", ".bash",
            // Windows
            ".exe", ".bat", ".cmd"
    };

    /**
     * Tries to find an executable with the given name. It first checks in of the given directories (if any) and
     * then tries in the system's path. The lookup tries several extensions (.sh, .bash, .exe. .cmd. and .bat).
     *
     * @param executable the name of the program to find, generally without the extension
     * @param dirs       optional set of directories in which the program need to be searched before checking the system's
     *                   path.
     * @return the executable's file, {@code null} if not found
     */
    public static File find(String executable, File... dirs) {
        // First try using the hints.
        if (dirs != null) {
            for (File hint : dirs) {
                File file = findExecutableInDirectory(executable, hint);
                if (file != null) {
                    return file;
                }
            }
        }

        // Not found, try to use the system path.
        return findExecutableInSystemPath(executable);
    }

    /**
     * Tries to find the given executable (specified by its name) in the given directory. It checks for a file having
     * one of the extensions contained in {@link #EXECUTABLE_EXTENSIONS}, and checks that this file is executable.
     *
     * @param executable the name of the program to find, generally without the extension
     * @param directory  the directory in which the program is searched.
     * @return the file of the program to be searched for if found. {@code null} otherwise. If the given directory is
     * {@code null} or not a real directory, it also returns {@code null}.
     */
    public static File findExecutableInDirectory(String executable, File directory) {
        if (directory == null || !directory.isDirectory()) {
            // if the directory is null or not a directory => returning null.
            return null;
        }
        for (String extension : EXECUTABLE_EXTENSIONS) {
            File file = new File(directory, executable + extension);
            if (file.isFile() && file.canExecute()) {
                return file;
            }
        }
        // Not found.
        return null;
    }

    /**
     * Tries to find the given executable (specified by its name) in the system's path. It checks for a file having
     * one of the extensions contained in {@link #EXECUTABLE_EXTENSIONS}, and checks that this file is executable.
     *
     * @param executable the name of the program to find, generally without the extension
     * @return the file of the program to be searched for if found. {@code null} otherwise.
     */
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
                File file = findExecutableInDirectory(executable, dir);
                if (file != null) {
                    return file;
                }
            }
        }
        // :-(
        return null;
    }

    /**
     * Checks whether the current operating system is Windows.
     * This check uses the {@literal os.name} system property.
     *
     * @return {@code true} if the os is windows, {@code false} otherwise.
     */
    public static boolean isWindows() {
        return isWindows(System.getProperty("os.name"));
    }

    /**
     * Checks whether the given operating system name is Windows.
     *
     * @param os the operating system name (value of the {@code os.name} system property
     * @return {@code true} if the os is windows, {@code false} otherwise.
     */
    public static boolean isWindows(String os) {
        return os != null && os.toLowerCase().contains("win");
    }

    /**
     * Checks whether the current operating system is Mac.
     * This check uses the {@literal os.name} system property.
     *
     * @return {@code true} if the os is Mac OS, {@code false} otherwise.
     */
    public static boolean isMac() {
        return isMac(System.getProperty("os.name"));
    }

    /**
     * Checks whether the given operating system name is Mac OS.
     *
     * @param os the operating system name (value of the {@code os.name} system property
     * @return {@code true} if the os is Mac OS, {@code false} otherwise.
     */
    public static boolean isMac(String os) {
        return os != null && os.toLowerCase().contains("mac");
    }

    /**
     * Checks whether the current operating system is Linux, Unix, or AIX.
     * This check uses the {@literal os.name} system property.
     * <p>
     * {@code true} if the os is Linux or Unix or AIX, {@code false} otherwise.
     */
    public static boolean isLinux() {
        return isLinux(System.getProperty("os.name"));
    }

    /**
     * Checks whether the given operating system name is Linux, Unix, or AIX.
     *
     * @param os the operating system name (value of the {@code os.name} system property
     * @return {@code true} if the os is Linux, Unix, or AIX, {@code false} otherwise.
     */
    public static boolean isLinux(String os) {
        if (os == null) {
            return false;
        }
        String operating = os.toLowerCase();
        return operating.contains("nix") || operating.contains("nux") || operating.contains("aix");
    }

    /**
     * Checks if the CPU architecture of the current computer is 64 bits.
     *
     * @return {@code true} if the CPU architecture of the current computer is 64 bits. {@code false} otherwise. This
     * methods checks first the {@literal sun.arch.data.model} system's property and then the {@literal os.arch} one.
     * If none of these two properties are set, it returns {@code false}.
     */
    public static boolean is64bits() {
        String arch = System.getProperty("sun.arch.data.model");
        if (Strings.isNullOrEmpty(arch)) {
            arch = System.getProperty("os.arch");
        }
        return is64bits(arch);
    }

    /**
     * Checks if the CPU architecture of the current computer is 64 bits.
     *
     * @param arch the architecture
     * @return {@code true} if the given CPU architecture is 64 bits. {@code false} otherwise.
     */
    public static boolean is64bits(String arch) {
        return arch != null && arch.contains("64");
    }

    /**
     * Checks whether the CPU of the current computer is an ARM CPU.
     *
     * @return {@code true} if the current computer use a ARM CPU, {@code false } otherwise. The check is based on
     * the {@literal os.arch} system property.
     */
    public static boolean isARM() {
        final String arch = System.getProperty("os.arch").toLowerCase();
        return isARM(arch);
    }

    /**
     * Checks whether the given CPU is an ARM CPU.
     *
     * @param arch the architecture of the CPU
     * @return {@code true} if the current computer use a ARM CPU, {@code false } otherwise.
     */
    protected static boolean isARM(String arch) {
        return arch.toLowerCase().contains("arm");
    }
}
