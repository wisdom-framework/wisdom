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
import org.apache.maven.project.MavenProject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Maven metadata to OSGi metadata.
 * (from the maven-bundle-plugin)
 * <p>
 * This class is a simplified version of the original class.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id: DefaultMaven2OsgiConverter.java 661727 2008-05-30 14:21:49Z bentmann $
 */
public class DefaultMaven2OsgiConverter {

    /**
     * Computes the file name of the bundle used in Wisdom distribution for the given Maven coordinates.
     * This convention is based on the uniqueness at runtime of 'bsn-version' (bsn is the bundle symbolic name).
     *
     * @param groupId    the groupId
     * @param artifactId the artifactId
     * @param version    the version
     * @return the computed name, composed by the symbolic name and the version: {@code bsn-version.jar}
     */
    public static String getBundleFileName(String groupId, String artifactId, String version) {
        return DefaultMaven2OsgiConverter.getBundleSymbolicName(groupId, artifactId) + "-" + version + ".jar";
    }

    /**
     * Computes the file name of the bundle used in Wisdom distribution for the given Maven project.
     * This convention is based on the uniqueness at runtime of 'bsn-version' (bsn is the bundle symbolic name).
     *
     * @param project the project
     * @return the computed name, composed by the symbolic name and the version: {@code bsn-version.jar}
     */
    public static String getBundleFileName(MavenProject project) {
        return getBundleFileName(project.getGroupId(), project.getArtifactId(), project.getVersion());
    }

    /**
     * Computes the file name of the bundle used in Wisdom distribution for the given Maven artifact.
     * This convention is based on the uniqueness at runtime of 'bsn-version' (bsn is the bundle symbolic name).
     *
     * @param artifact the Maven artifact
     * @return the computed name, composed by the symbolic name and the version: {@code bsn-version.jar}
     */
    public static String getBundleFileName(Artifact artifact) {
        return getBundleFileName(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
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
     *
     * @param artifact the Maven artifact
     * @return the symbolic name for the given artifact
     */
    public static String getBundleSymbolicName(Artifact artifact) {
        return getBundleSymbolicName(artifact.getGroupId(), artifact.getArtifactId());
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
     *
     * @param groupId    the groupId
     * @param artifactId the artifactId
     * @return the symbolic name for the given artifact coordinates
     */
    public static String getBundleSymbolicName(String groupId, String artifactId) {
        int i = groupId.lastIndexOf('.');

        String lastSection = groupId.substring(++i);
        if (artifactId.equals(lastSection)) {
            return groupId;
        }

        if (artifactId.equals(groupId)
                || artifactId.startsWith(groupId + ".")) {
            return artifactId;
        }

        if (artifactId.startsWith(lastSection)) {
            artifactId = artifactId.substring(lastSection.length());
            if (!Character.isLetterOrDigit(artifactId.charAt(0))) {
                return (groupId + "." + artifactId.substring(1)).replace("-", ".");
            }
            // Else fall to the default case.
        }
        return (groupId + "." + artifactId).replace("-", ".");
    }


    /**
     * Computes the OSGi-compliant version for the given Maven artifact's version.
     *
     * @param version the version of a Maven artifact
     * @return the OSGi version computed from the given Maven version
     */
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

    /**
     * Cleans up the version to be OSGi compliant.
     *
     * @param version a Maven version
     * @return the OSGi version computed from the given Maven version.
     */
    public static String cleanupVersion(String version) {
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
