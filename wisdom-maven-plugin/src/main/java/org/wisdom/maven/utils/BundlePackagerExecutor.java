package org.wisdom.maven.utils;

import aQute.lib.osgi.*;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Packages the bundle using BND. Have to be as close as possible from the Wisdom Test Probe Maker.
 */
public class BundlePackagerExecutor {
    public static final String INSTRUCTIONS_FILE = "src/main/osgi/osgi.bnd";
    private static final String MAVEN_SYMBOLICNAME = "maven-symbolicname";

    public static void execute(AbstractWisdomMojo mojo, File output) throws Exception {
        Properties properties = getDefaultProperties(mojo, mojo.project);
        boolean provided = readInstructionsFromBndFiles(properties, mojo.basedir);
        if (! provided) {
            // Using defaults if there are no bnd file.
            populatePropertiesWithDefaults(mojo.basedir, properties);
        }

        Builder builder = getOSGiBuilder(mojo, properties, computeClassPath(mojo));
        buildOSGiBundle(builder);
        reportErrors("BND ~> ", builder.getWarnings(), builder.getErrors());
        File bnd = File.createTempFile("bnd-", ".jar");
        File ipojo = File.createTempFile("ipojo-", ".jar");
        builder.getJar().write(bnd);

        Pojoization pojoization = new Pojoization();
        pojoization.pojoization(bnd, ipojo, new File(mojo.basedir, "src/main/resources"));
        reportErrors("iPOJO ~> ", pojoization.getWarnings(), pojoization.getErrors());

        Files.move(Paths.get(ipojo.getPath()),Paths.get(output.getPath()),StandardCopyOption.REPLACE_EXISTING);
    }

    private static void populatePropertiesWithDefaults(File basedir, Properties properties) throws IOException {
        List<String> privates = new ArrayList<>();
        List<String> exports = new ArrayList<>();

        File classes = new File(basedir, "target/classes");

        Set<String> packages = new LinkedHashSet<>();
        if (classes.isDirectory()) {
            Jar jar = new Jar(".", classes);
            packages.addAll(jar.getPackages());
        }

        for (String s : packages) {
            if (s.endsWith("service") || s.endsWith("services")) {
                exports.add(s);
            } else {
                if (! s.isEmpty()) {
                    privates.add(s + ";-split-package:=merge-first");
                }
            }
        }

        properties.put(Constants.PRIVATE_PACKAGE, toClause(privates));
        if (!exports.isEmpty()) {
            properties.put(Constants.EXPORT_PACKAGE, toClause(privates));
        }

        // Already set.
        // properties.put(Constants.IMPORT_PACKAGE, "*");
    }

    private static String toClause(List<String> packages) {
        StringBuilder builder = new StringBuilder();
        for (String p : packages) {
            if (builder.length() != 0) {
                builder.append(", ");
            }
            builder.append(p);
        }
        return builder.toString();
    }

    private static Jar[] computeClassPath(AbstractWisdomMojo mojo) throws IOException {
        List<Jar> list = new ArrayList<>();
        File classes = new File(mojo.basedir, "target/classes");

        if (classes.isDirectory()) {
            list.add(new Jar(".", classes));
        }

        for (Artifact artifact : mojo.project.getArtifacts()) {
            if (artifact.getArtifactHandler().isAddedToClasspath()) {
                if (!Artifact.SCOPE_TEST.equals(artifact.getScope())) {
                    File file = artifact.getFile();
                    if (file == null) {
                        mojo.getLog().warn(
                                "File is not available for artifact " + artifact + " in project "
                                        + mojo.project.getArtifact());
                        continue;
                    }
                    Jar jar = new Jar(artifact.getArtifactId(), file);
                    list.add(jar);
                }
            }
        }

        Jar[] cp = new Jar[list.size()];
        list.toArray(cp);

        return cp;

    }

    protected static Builder getOSGiBuilder(AbstractWisdomMojo mojo, Properties properties,
                                            Jar[] classpath) throws Exception {
        Builder builder = new Builder();
        synchronized (BundlePackagerExecutor.class) {
            builder.setBase(mojo.basedir);
        }
        builder.setProperties(sanitize(properties));
        if (classpath != null) {
            builder.setClasspath(classpath);
        }
        return builder;
    }

    private static boolean readInstructionsFromBndFiles(Properties properties, File basedir) throws IOException {
        Properties props = new Properties();
        File instructionFile = new File(basedir, INSTRUCTIONS_FILE);
        if (instructionFile.isFile()) {
            InputStream is = null;
            try {
                is = new FileInputStream(instructionFile);
                props.load(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            return false;
        }

        // Insert in the given properties to the list of properties.
        @SuppressWarnings("unchecked") Enumeration<String> names = (Enumeration<String>) props.propertyNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            properties.put(key, props.getProperty(key));
        }

        return true;
    }

    protected static Properties sanitize(Properties properties) {
        // convert any non-String keys/values to Strings
        Properties sanitizedEntries = new Properties();
        for (Iterator itr = properties.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry entry = (Map.Entry) itr.next();
            if (!(entry.getKey() instanceof String)) {
                String key = sanitize(entry.getKey());
                if (!properties.containsKey(key)) {
                    sanitizedEntries.setProperty(key, sanitize(entry.getValue()));
                }
                itr.remove();
            } else if (!(entry.getValue() instanceof String)) {
                entry.setValue(sanitize(entry.getValue()));
            }
        }
        properties.putAll(sanitizedEntries);
        return properties;
    }

    protected static String sanitize(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Iterable) {
            String delim = "";
            StringBuilder buf = new StringBuilder();
            for (Object i : (Iterable<?>) value) {
                buf.append(delim).append(i);
                delim = ", ";
            }
            return buf.toString();
        } else if (value.getClass().isArray()) {
            String delim = "";
            StringBuilder buf = new StringBuilder();
            for (int i = 0, len = Array.getLength(value); i < len; i++) {
                buf.append(delim).append(Array.get(value, i));
                delim = ", ";
            }
            return buf.toString();
        } else {
            return String.valueOf(value);
        }
    }

    protected static Builder buildOSGiBundle(Builder builder) throws Exception {
        builder.build();
        return builder;
    }

    protected static boolean reportErrors(String prefix, List<String> warnings, List<String> errors) {
        for (String msg : warnings) {
            System.err.println(prefix + " : " + msg);
        }

        boolean hasErrors = false;
        String fileNotFound = "Input file does not exist: ";
        for (String msg : errors) {
            if (msg.startsWith(fileNotFound) && msg.endsWith("~")) {
                // treat as warning; this error happens when you have duplicate entries in Include-Resource
                String duplicate = Processor.removeDuplicateMarker(msg.substring(fileNotFound.length()));
                System.err.println(prefix + " Duplicate path '" + duplicate + "' in Include-Resource");
            } else {
                System.err.println(prefix + " : " + msg);
                hasErrors = true;
            }
        }
        return hasErrors;
    }

    private static void header(Properties properties, String key, Object value) {
        if (value == null)
            return;

        if (value instanceof Collection && ((Collection) value).isEmpty())
            return;

        properties.put(key, value.toString().replaceAll("[\r\n]", ""));
    }

    private static Map getProperties(Model projectModel, String prefix) {
        Map properties = new LinkedHashMap();
        Method methods[] = Model.class.getDeclaredMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get")) {
                try {
                    Object v = method.invoke(projectModel, null);
                    if (v != null) {
                        name = prefix + Character.toLowerCase(name.charAt(3)) + name.substring(4);
                        if (v.getClass().isArray())
                            properties.put(name, Arrays.asList((Object[]) v).toString());
                        else
                            properties.put(name, v);
                    }
                } catch (Exception e) {  //NOSONAR
                    // too bad
                }
            }
        }
        return properties;
    }

    private static StringBuffer printLicenses(List licenses) {
        if (licenses == null || licenses.size() == 0)
            return null;
        StringBuffer sb = new StringBuffer();
        String del = "";
        for (Object license : licenses) {
            License l = (License) license;
            String url = l.getUrl();
            if (url == null)
                continue;
            sb.append(del);
            sb.append(url);
            del = ", ";
        }
        if (sb.length() == 0)
            return null;
        return sb;
    }


    protected static Properties getDefaultProperties(AbstractWisdomMojo mojo, MavenProject currentProject) {
        Properties properties = new Properties();
        DefaultMaven2OsgiConverter converter = new DefaultMaven2OsgiConverter();
        String bsn;
        try {
            bsn = converter.getBundleSymbolicName(currentProject.getArtifact());
        } catch (Exception e) { //NOSONAR
            bsn = currentProject.getGroupId() + "." + currentProject.getArtifactId();
        }

        // Setup defaults
        properties.put(MAVEN_SYMBOLICNAME, bsn);
        properties.put(Analyzer.BUNDLE_SYMBOLICNAME, bsn);
        properties.put(Analyzer.IMPORT_PACKAGE, "*");
        properties.put(Analyzer.BUNDLE_VERSION, converter.getVersion(currentProject.getVersion()));

        // remove the extraneous Include-Resource and Private-Package entries from generated manifest
        properties.put(Constants.REMOVEHEADERS, Analyzer.INCLUDE_RESOURCE + ',' + Analyzer.PRIVATE_PACKAGE);

        header(properties, Analyzer.BUNDLE_DESCRIPTION, currentProject.getDescription());
        StringBuffer licenseText = printLicenses(currentProject.getLicenses());
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

        if (mojo.session != null) {
            try {
                // don't pass upper-case session settings to bnd as they end up in the manifest
                Properties sessionProperties = mojo.session.getExecutionProperties();
                for (Enumeration e = sessionProperties.propertyNames(); e.hasMoreElements(); ) {
                    String key = (String) e.nextElement();
                    if (key.length() > 0 && !Character.isUpperCase(key.charAt(0))) {
                        properties.put(key, sessionProperties.getProperty(key));
                    }
                }
            } catch (Exception e) {
                mojo.getLog().warn("Problem with Maven session properties: " + e.getMessage(), e);
            }
        }

        properties.putAll(getProperties(currentProject.getModel(), "project.build."));
        properties.putAll(getProperties(currentProject.getModel(), "pom."));
        properties.putAll(getProperties(currentProject.getModel(), "project."));

        properties.put("project.baseDir", mojo.basedir);
        properties.put("project.build.directory", mojo.buildDirectory);
        properties.put("project.build.outputdirectory", new File(mojo.buildDirectory, "classes"));

        return properties;
    }
}
