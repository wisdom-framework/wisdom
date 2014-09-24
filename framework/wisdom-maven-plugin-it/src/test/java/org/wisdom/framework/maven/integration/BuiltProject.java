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
package org.wisdom.framework.maven.integration;


import org.apache.commons.io.FileUtils;
import org.wisdom.maven.utils.DefaultMaven2OsgiConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BuiltProject {

    public static final String GROUP_ID = "org.wisdom-framework.test";
    public static final String ARTIFACT_ID = "acme-project";
    public static final String VERSION = "1.0-testing";

    private final File root;

    public BuiltProject(File root) {
        this.root = root;
    }

    public File log() {
        return new File(root, "build.log");
    }

    public String getLogAsString() {
        if (!log().isFile()) {
            return null;
        }
        try {
            return FileUtils.readFileToString(log());
        } catch (IOException e) {
            return null;
        }
    }

    public List<String> getLogLines() {
        if (!log().isFile()) {
            return null;
        }
        try {
            return FileUtils.readLines(log());
        } catch (IOException e) {
            return null;
        }
    }

    public void assertErrorFreeLog()
            throws AssertionError {
        List<String> lines = getLogLines();
        assertThat(lines).isNotNull();

        for (String line : lines) {
            // A hack to keep stupid velocity resource loader errors from triggering failure
            if (line.contains("[ERROR]") && !isVelocityError(line)) {
                throw new AssertionError("Error in execution: " + line);
            }
        }
    }

    /**
     * Checks whether the specified line is just an error message from Velocity. Especially old versions of Doxia employ
     * a very noisy Velocity instance.
     *
     * @param line The log line to check, must not be <code>null</code>.
     * @return <code>true</code> if the line appears to be a Velocity error, <code>false</code> otherwise.
     */
    private static boolean isVelocityError(String line) {
        return line.contains("VM_global_library.vm") || line.contains("VM #") && line.contains("macro");
    }

    /**
     * Throws an exception if the text is not present in the log.
     *
     * @param text the text to assert present
     * @throws java.lang.AssertionError
     */
    public void assertContainedInLog(String text)
            throws AssertionError {
        List<String> lines = getLogLines();
        assertThat(lines).isNotNull();

        boolean result = false;
        for (String line : lines) {
            if (line.contains(text)) {
                result = true;
                break;
            }
        }
        if (!result) {
            throw new AssertionError("Text not found in log: " + text);
        }
    }


    public boolean wasBuilt() {
        return log().isFile();
    }

    public File pom() {
        return new File(root, "pom.xml");
    }

    public File target() {
        return new File(root, "target");
    }

    public File wisdom() {
        return new File(target(), "wisdom");
    }

    public File app() {
        return new File(wisdom(), "application");
    }

    public File assets() {
        return new File(wisdom(), "assets");
    }

    public String getSymbolicName() {
        return DefaultMaven2OsgiConverter.getBundleSymbolicName(GROUP_ID, ARTIFACT_ID);
    }

    public String getArtifactName() {
        return ARTIFACT_ID + "-" + VERSION;
    }

    public String getBundleName() {
        return getSymbolicName() + "-" + VERSION + ".jar";
    }

    public String getBundleArtifactName() {
        return getArtifactName() + ".jar";
    }

    public String getDistributionArtifactName() {
        return getArtifactName() + ".zip";
    }

}
