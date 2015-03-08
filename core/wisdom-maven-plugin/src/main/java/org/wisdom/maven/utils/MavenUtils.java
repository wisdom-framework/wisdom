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

import aQute.bnd.osgi.Analyzer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A set of utilities method to handle Maven metadata.
 */
public class MavenUtils {

    public static final String MAVEN_SYMBOLICNAME = "maven-symbolicname";

    /**
     * Gets the default set of properties for the given project.
     *
     * @param currentProject the project
     * @return the set of properties, containing default bundle packaging instructions.
     */
    public static Properties getDefaultProperties(MavenProject currentProject) {
        Properties properties = new Properties();
        String bsn = DefaultMaven2OsgiConverter.getBundleSymbolicName(currentProject.getArtifact());

        // Setup defaults
        properties.put(MAVEN_SYMBOLICNAME, bsn);
        properties.put("bundle.file.name", DefaultMaven2OsgiConverter.getBundleFileName(currentProject.getArtifact()));
        properties.put(Analyzer.BUNDLE_SYMBOLICNAME, bsn);
        properties.put(Analyzer.IMPORT_PACKAGE, "*");
        properties.put(Analyzer.BUNDLE_VERSION, DefaultMaven2OsgiConverter.getVersion(currentProject.getVersion()));

        header(properties, Analyzer.BUNDLE_DESCRIPTION, currentProject.getDescription());
        StringBuilder licenseText = printLicenses(currentProject.getLicenses());
        if (licenseText != null) {
            header(properties, Analyzer.BUNDLE_LICENSE, licenseText);
        }
        header(properties, Analyzer.BUNDLE_NAME, currentProject.getName());

        if (currentProject.getOrganization() != null) {
            if (currentProject.getOrganization().getName() != null) {
                String organizationName = currentProject.getOrganization().getName();
                header(properties, Analyzer.BUNDLE_VENDOR, organizationName);
                properties.put("project.organization.name", organizationName);
                properties.put("pom.organization.name", organizationName);
            }
            if (currentProject.getOrganization().getUrl() != null) {
                String organizationUrl = currentProject.getOrganization().getUrl();
                header(properties, Analyzer.BUNDLE_DOCURL, organizationUrl);
                properties.put("project.organization.url", organizationUrl);
                properties.put("pom.organization.url", organizationUrl);
            }
        }

        properties.putAll(currentProject.getModel().getProperties());

        for (String s : currentProject.getFilters()) {
            File filterFile = new File(s);
            if (filterFile.isFile()) {
                properties.putAll(PropertyUtils.loadProperties(filterFile));
            }
        }

        properties.putAll(getProperties(currentProject.getModel(), "project.build."));
        properties.putAll(getProperties(currentProject.getModel(), "pom."));
        properties.putAll(getProperties(currentProject.getModel(), "project."));

        properties.put("project.baseDir", currentProject.getBasedir().getAbsolutePath());
        properties.put("project.build.directory", currentProject.getBuild().getDirectory());
        properties.put("project.build.outputdirectory", currentProject.getBuild().getOutputDirectory());

        properties.put("project.source.roots", getArray(currentProject.getCompileSourceRoots()));
        properties.put("project.testSource.roots", getArray(currentProject.getTestCompileSourceRoots()));

        properties.put("project.resources", toString(currentProject.getResources()));
        properties.put("project.testResources", toString(currentProject.getTestResources()));

        return properties;
    }

    /**
     * Compute a String form the given list of paths. The list uses comma as separator.
     *
     * @param paths the list of path
     * @return the computed String
     */
    protected static String getArray(List<String> paths) {
        StringBuilder builder = new StringBuilder();

        for (String s : paths) {
            if (builder.length() == 0) {
                builder.append(s);
            } else {
                builder.append(",").append(s);
            }
        }

        return builder.toString();
    }

    /**
     * Compute a String form the given list of resources. The list is structured as follows:
     * list:=resource[,resource]*
     * resource:=directory;target;filtering;
     *
     * @param resources the list of resources
     * @return the computed String form
     */
    protected static String toString(List<Resource> resources) {
        StringBuilder builder = new StringBuilder();

        for (Resource resource : resources) {
            if (builder.length() == 0) {
                builder.append(resource.getDirectory())
                        .append(";")
                        .append(resource.getTargetPath() != null ? resource.getTargetPath() : "")
                        .append(";")
                        .append(resource.getFiltering() != null ? resource.getFiltering() : "true")
                        .append(";");
            } else {
                builder.append(",")
                        .append(resource.getDirectory())
                        .append(";")
                        .append(resource.getTargetPath() != null ? resource.getTargetPath() : "")
                        .append(";")
                        .append(resource.getFiltering() != null ? resource.getFiltering() : "true")
                        .append(";");
            }
        }

        return builder.toString();
    }

    private static Map getProperties(Model projectModel, String prefix) {
        Map<String, String> properties = new LinkedHashMap<>();
        Method[] methods = Model.class.getDeclaredMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get")) {
                try {
                    Object v = method.invoke(projectModel);
                    if (v != null) {
                        name = prefix + Character.toLowerCase(name.charAt(3)) + name.substring(4);
                        if (v.getClass().isArray()) {
                            properties.put(name, Arrays.asList((Object[]) v).toString());
                        } else {
                            properties.put(name, v.toString());
                        }
                    }
                } catch (Exception e) {  //NOSONAR
                    // too bad
                }
            }
        }
        return properties;
    }

    private static StringBuilder printLicenses(List licenses) {
        if (licenses == null || licenses.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String del = "";
        for (Object license : licenses) {
            License l = (License) license;
            String url = l.getUrl();
            if (url == null) {
                continue;
            }
            sb.append(del);
            sb.append(url);
            del = ", ";
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb;
    }

    private static void header(Properties properties, String key, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof Collection && ((Collection) value).isEmpty()) {
            return;
        }

        properties.put(key, value.toString().replaceAll("[\r\n]", ""));
    }
}
