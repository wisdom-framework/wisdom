package org.wisdom.maven.utils;

import aQute.bnd.osgi.Analyzer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;
import org.wisdom.maven.Constants;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A set of utilities method to handle Maven metadata.
 */
public class MavenUtils {

    public static final String MAVEN_SYMBOLICNAME = "maven-symbolicname";


    public static void dumpDependencies(MavenProject project) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.createArrayNode();
        for (Artifact artifact : project.getArtifacts()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("groupId", artifact.getGroupId());
            node.put("artifactId", artifact.getArtifactId());
            node.put("version", artifact.getVersion());
            if (artifact.getClassifier() != null) {
                node.put("classifier", artifact.getClassifier());
            }
            node.put("scope", artifact.getScope());
            node.put("file", artifact.getFile().getAbsolutePath());
            array.add(node);
        }

        File dependencies = new File(project.getBasedir(), Constants.DEPENDENCIES_FILE);
        FileUtils.forceMkdir(dependencies.getParentFile());
        mapper.writerWithDefaultPrettyPrinter().writeValue(dependencies, array);

    }

    public static Properties getDefaultProperties(AbstractWisdomMojo mojo, MavenProject currentProject) {
        Properties properties = new Properties();
        String bsn = DefaultMaven2OsgiConverter.getBundleSymbolicName(currentProject.getArtifact());

        // Setup defaults
        properties.put(MAVEN_SYMBOLICNAME, bsn);
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

        properties.putAll(currentProject.getProperties());
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

        properties.put("project.baseDir", mojo.basedir.getAbsolutePath());
        properties.put("project.build.directory", mojo.buildDirectory.getAbsolutePath());
        properties.put("project.build.outputdirectory", new File(mojo.buildDirectory, "classes").getAbsolutePath());

        return properties;
    }

    private static Map getProperties(Model projectModel, String prefix) {
        Map<String, String> properties = new LinkedHashMap<>();
        Method methods[] = Model.class.getDeclaredMethods();
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
        if (licenses == null || licenses.size() == 0) {
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
