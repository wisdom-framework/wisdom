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

import org.apache.maven.artifact.Artifact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Maven metadata to OSGi metadata.
 * (from the maven-bundle-plugin)
 *
 * This class is a simplified version of the original class.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id: DefaultMaven2OsgiConverter.java 661727 2008-05-30 14:21:49Z bentmann $
 */
public class DefaultMaven2OsgiConverter {

    /**
     * Build the symbolic name from the groupId and artifactId.
     * @param groupId the groupId
     * @param artifactId the artifactId
     * @return the symbolic name composed by appending the artifactId to the groupId and replacing all '-' by '.'.
     */
    public static String getBundleSymbolicName(String groupId, String artifactId) {
        return (groupId + "." + artifactId).replace("-", ".");
    }


    /**
     * Get the symbolic name as groupId + "." + artifactId, with the following exceptions. Unlike the original method
     * from the Maven Bundle Plugin, this method does not use the bundle inspection,
     * as the artifact's file does not exist when this method is called.
     * <ul>
     * <li>if artifactId is equal to last section of groupId then groupId is returned. eg.
     * org.apache.maven:maven -> org.apache.maven</li>
     * <li>if artifactId starts with last section of groupId that portion is removed. eg.
     * org.apache.maven:maven-core -> org.apache.maven.core</li>
     * <li>if artifactId starts with groupId then the artifactId is removed. eg.
     * org.apache:org.apache.maven.core -> org.apache.maven.core</li>
     * </ul>
     */
    public static String getBundleSymbolicName(Artifact artifact) {
        int i = artifact.getGroupId().lastIndexOf('.');

        String lastSection = artifact.getGroupId().substring(++i);
        if (artifact.getArtifactId().equals(lastSection)) {
            return artifact.getGroupId();
        }

        if (artifact.getArtifactId().equals(artifact.getGroupId())
                || artifact.getArtifactId().startsWith(artifact.getGroupId() + ".")) {
            return artifact.getArtifactId();
        }

        if (artifact.getArtifactId().startsWith(lastSection)) {
            String artifactId = artifact.getArtifactId().substring(lastSection.length());
            if (! Character.isLetterOrDigit(artifactId.charAt(0))) {
                return getBundleSymbolicName(artifact.getGroupId(), artifactId.substring(1));
            }
            // Else fall to the default case.
        }

        return getBundleSymbolicName(artifact.getGroupId(), artifact.getArtifactId());
    }


    public static String getVersion(String version) {
        return cleanupVersion(version);
    }

    /**
     * Clean up version parameters. Other builders use more fuzzy definitions of
     * the version syntax. This method cleans up such a version to match an OSGi
     * version.
     */
    static final Pattern FUZZY_VERSION = Pattern.compile("(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([^a-zA-Z0-9](.*))?",
            Pattern.DOTALL);


    static public String cleanupVersion(String version) {
        StringBuilder result = new StringBuilder();
        Matcher m = FUZZY_VERSION.matcher(version);
        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(3);
            String micro = m.group(5);
            String qualifier = m.group(7);

            if (major != null) {
                result.append(major);
                if (minor != null) {
                    result.append(".");
                    result.append(minor);
                    if (micro != null) {
                        result.append(".");
                        result.append(micro);
                        if (qualifier != null) {
                            result.append(".");
                            cleanupModifier(result, qualifier);
                        }
                    } else if (qualifier != null) {
                        result.append(".0.");
                        cleanupModifier(result, qualifier);
                    } else {
                        result.append(".0");
                    }
                } else if (qualifier != null) {
                    result.append(".0.0.");
                    cleanupModifier(result, qualifier);
                } else {
                    result.append(".0.0");
                }
            }
        } else {
            result.append("0.0.0.");
            cleanupModifier(result, version);
        }
        return result.toString();
    }


    static void cleanupModifier(StringBuilder result, String modifier) {
        for (int i = 0; i < modifier.length(); i++) {
            char c = modifier.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || c == '-') { //NOSONAR
                result.append(c);
            } else {
                result.append('_');
            }
        }
    }

}
