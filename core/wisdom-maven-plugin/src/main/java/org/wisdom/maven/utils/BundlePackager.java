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

import aQute.bnd.osgi.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.apache.felix.ipojo.manipulator.util.Classpath;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Packages the bundle using BND.
 */
public final class BundlePackager implements org.wisdom.maven.Constants {

    private BundlePackager() {
        //Hide default constructor
    }

    /**
     * Creates the bundle.
     *
     * @param basedir the project's base directory
     * @param output  the output file
     * @throws IOException occurs when the bundle cannot be built correctly.
     */
    public static void bundle(File basedir, File output) throws IOException {
        Properties properties = new Properties();
        // Loads the properties inherited from Maven.
        readMavenProperties(basedir, properties);
        // Loads the properties from the BND file.
        boolean provided = readInstructionsFromBndFiles(properties, basedir);
        if (!provided) {
            // No bnd files, set default valued
            populatePropertiesWithDefaults(basedir, properties);
        }

        // Integrate custom headers added by other plugins.
        mergeExtraHeaders(basedir, properties);

        // Instruction loaded, start the build sequence.
        final Jar[] jars = computeClassPath(basedir);
        final Set<String> elements = computeClassPathElement(basedir);

        File bnd = null;
        File ipojo = null;
        try {
            Builder builder = getOSGiBuilder(basedir, properties, jars);
            builder.build();

            reportErrors("BND ~> ", builder.getWarnings(), builder.getErrors());
            bnd = File.createTempFile("bnd-", ".jar");
            ipojo = File.createTempFile("ipojo-", ".jar");
            builder.getJar().write(bnd);
        } catch (Exception e) {
            throw new IOException("Cannot build the OSGi bundle", e);
        }

        Classpath classpath = new Classpath(elements);
        Pojoization pojoization = new Pojoization();
        pojoization.pojoization(bnd, ipojo, new File(basedir, "src/main/resources"), classpath.createClassLoader());
        reportErrors("iPOJO ~> ", pojoization.getWarnings(), pojoization.getErrors());

        Files.move(Paths.get(ipojo.getPath()), Paths.get(output.getPath()), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * If a bundle has added extra headers, they are added to the bundle manifest.
     *
     * @param baseDir    the project directory
     * @param properties the current set of properties in which the read metadata are written
     */
    private static void mergeExtraHeaders(File baseDir, Properties properties) throws IOException {
        File extra = new File(baseDir, EXTRA_HEADERS_FILE);
        merge(properties, extra);
    }

    private static void merge(Properties properties, File extra) throws IOException {
        if (extra.isFile()) {
            FileInputStream fis = null;
            try {
                Properties headers = new Properties();
                fis = new FileInputStream(extra);
                headers.load(fis);
                properties.putAll(headers);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
    }

    /**
     * This method is used by plugin willing to add custom header to the bundle manifest.
     *
     * @param baseDir the project directory
     * @param header  the header to add
     * @param value   the value to write
     * @throws IOException if the header cannot be added
     */
    public static void addExtraHeaderToBundleManifest(File baseDir, String header, String value) throws IOException {
        Properties props = new Properties();
        File extra = new File(baseDir, EXTRA_HEADERS_FILE);
        extra.getParentFile().mkdirs();
        // If the file exist it loads it, if not nothing happens.
        merge(props, extra);
        if (value != null) {
            props.setProperty(header, value);
        } else {
            props.remove(header);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(extra);
            props.store(fos, "");
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    /**
     * We should have generated a target/osgi/osgi.properties file will all the metadata we inherit from Maven.
     *
     * @param baseDir    the project directory
     * @param properties the current set of properties in which the read metadata are written
     */
    private static void readMavenProperties(File baseDir, Properties properties) throws IOException {
        File osgi = new File(baseDir, org.wisdom.maven.Constants.OSGI_PROPERTIES);
        merge(properties, osgi);
    }

    public static String getPackageName(String filename) {
        int n = filename.lastIndexOf(File.separatorChar);
        return n < 0 ? "." : filename.substring(0, n).replace(File.separatorChar, '.');
    }


    private static void populatePropertiesWithDefaults(File basedir, Properties properties) throws IOException {
        List<String> privates = new ArrayList<>();
        List<String> exports = new ArrayList<>();

        // Do local resources
        String resources = getDefaultResources(properties, false);
        if (!resources.isEmpty()) {
            properties.put(Analyzer.INCLUDE_RESOURCE, resources);
        }

        File classes = new File(basedir, "target/classes");
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(classes);
        scanner.setIncludes(new String[]{"**/*.class"});
        scanner.addDefaultExcludes();
        scanner.scan();

        Set<String> packages = new LinkedHashSet<>();
        for (int i = 0; i < scanner.getIncludedFiles().length; i++) {
            packages.add(getPackageName(scanner.getIncludedFiles()[i]));
        }

        for (String s : packages) {
            if (shouldBeExported(s)) {
                exports.add(s);
            } else {
                if (!s.isEmpty() && !s.equals(".")) {
                    privates.add(s + ";-split-package:=merge-first");
                }
            }
        }

        properties.put(Constants.PRIVATE_PACKAGE, toClause(privates));
        if (!exports.isEmpty()) {
            properties.put(Constants.EXPORT_PACKAGE, toClause(exports));
        }

        // For debugging purpose, dump the instructions to target/osgi/default-instructions.properties
        FileOutputStream fos = null;
        try {
            File out = new File(basedir, "target/osgi/default-instructions.properties");
            fos = new FileOutputStream(out);
            properties.store(fos, "Default BND Instructions");
        } catch (IOException e) { // NOSONAR
            // Ignore it.
        } finally {
            IOUtils.closeQuietly(fos);
        }

    }

    public static String getDefaultResources(Properties properties, boolean test) {
        final Pattern pattern = Pattern.compile("(.*);(.*);(.*);");
        final String basePath = properties.getProperty("project.baseDir");
        List<String> resources = getSerializedResources(properties, test);

        String target = "target/classes";
        if (test) {
            target = "target/test-classes";
        }

        Set<String> pathSet = new LinkedHashSet<>();
        for (String resource : resources) {
            Matcher matcher = pattern.matcher(resource);
            if (!matcher.matches()) {
                continue;
            }
            String sourcePath = matcher.group(1);
            String targetPath = matcher.group(2);
            boolean filtered = Boolean.parseBoolean(matcher.group(3));

            // ignore empty or non-local resources
            if (new File(sourcePath).exists() && ((targetPath == null) || (!targetPath.contains("..")))) {
                DirectoryScanner scanner = new DirectoryScanner();

                scanner.setBasedir(sourcePath);
                scanner.setIncludes(new String[]{"**/**"});
                scanner.addDefaultExcludes();
                scanner.scan();

                List<String> includedFiles = Arrays.asList(scanner.getIncludedFiles());
                for (String name : includedFiles) {
                    String path;

                    if (filtered) {
                        // Try to find the filtered version of the file.
                        path = target + '/' + name;
                    } else {
                        path = sourcePath + '/' + name;
                    }

                    // make relative to project
                    if (path.startsWith(basePath)) {
                        if (path.length() == basePath.length()) {
                            path = ".";
                        } else {
                            path = path.substring(basePath.length() + 1);
                        }
                    }

                    // replace windows backslash with a slash
                    // this is a workaround for a problem with bnd 0.0.189
                    if (File.separatorChar != '/') {
                        name = name.replace(File.separatorChar, '/');
                        path = path.replace(File.separatorChar, '/');
                    }

                    // copy to correct place
                    path = name + '=' + path;
                    if (targetPath != null) {
                        path = targetPath + '/' + path;
                    }

                    pathSet.add(path);
                }
            }
        }

        StringBuilder resourcePaths = new StringBuilder();
        for (Iterator i = pathSet.iterator(); i.hasNext(); ) {
            resourcePaths.append(i.next());
            if (i.hasNext()) {
                resourcePaths.append(',');
            }
        }
        return resourcePaths.toString();
    }

    private static List<String> getSerializedResources(Properties properties, boolean test) {
        String input = !test ? properties.getProperty("project.resources")
                : properties.getProperty("project.testResources");
        if (Strings.isNullOrEmpty(input)) {
            return Collections.emptyList();
        }
        String[] resources = input.split(",");
        return Arrays.asList(resources);
    }

    /**
     * Checks whether the given package must be exported. The decision is made from heuristics.
     *
     * @param packageName the package name
     * @return {@literal true} if the package has to be exported, {@literal false} otherwise.
     */
    public static boolean shouldBeExported(String packageName) {
        boolean service = packageName.endsWith(".service");
        service = service
                || packageName.contains(".service.")
                || packageName.endsWith(".services")
                || packageName.contains(".services.");

        boolean api = packageName.endsWith(".api");
        api = api
                || packageName.contains(".api.")
                || packageName.endsWith(".apis")
                || packageName.contains(".apis.");

        boolean model = packageName.endsWith(".model");
        model = model
                || packageName.contains(".model.")
                || packageName.endsWith(".models")
                || packageName.contains(".models.");

        boolean entity = packageName.endsWith(".entity");
        entity = entity
                || packageName.contains(".entity.")
                || packageName.endsWith(".entities")
                || packageName.contains(".entities.");

        return !packageName.isEmpty() && !packageName.equals(".") && (service || api || model || entity);
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

    private static Jar[] computeClassPath(File basedir) throws IOException {
        List<Jar> list = new ArrayList<>();
        File classes = new File(basedir, "target/classes");

        if (classes.isDirectory()) {
            list.add(new Jar("", classes));
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.readValue(new File(basedir, DEPENDENCIES_FILE), ArrayNode.class);
        Iterator<JsonNode> items = array.elements();
        while (items.hasNext()) {
            ObjectNode node = (ObjectNode) items.next();
            String scope = node.get("scope").asText();
            if (!"test".equalsIgnoreCase(scope)) {
                File file = new File(node.get("file").asText());
                if (file.getName().endsWith(".jar")) {
                    Jar jar = new Jar(node.get("artifactId").asText(), file);
                    list.add(jar);
                }
                // If it's not a jar file - ignore it.
            }
        }
        Jar[] cp = new Jar[list.size()];
        list.toArray(cp);

        return cp;
    }

    private static Set<String> computeClassPathElement(File basedir) throws IOException {
        Set<String> list = new LinkedHashSet<>();
        File classes = new File(basedir, "target/classes");

        if (classes.isDirectory()) {
            list.add(classes.getAbsolutePath());
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.readValue(new File(basedir, DEPENDENCIES_FILE), ArrayNode.class);
        Iterator<JsonNode> items = array.elements();
        while (items.hasNext()) {
            ObjectNode node = (ObjectNode) items.next();
            String scope = node.get("scope").asText();
            if (!"test".equalsIgnoreCase(scope)) {
                File file = new File(node.get("file").asText());
                if (file.getName().endsWith(".jar")) {
                    list.add(file.getAbsolutePath());
                }
                // If it's not a jar file - ignore it.
            }
        }
        return list;
    }


    private static Builder getOSGiBuilder(File basedir, Properties properties,
                                          Jar[] classpath) {
        Builder builder = new Builder();
        synchronized (BundlePackager.class) {
            builder.setBase(basedir);
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

    private static Properties sanitize(Properties properties) {
        // convert any non-String keys/values to Strings
        Properties sanitizedEntries = new Properties();
        for (Iterator<?> itr = properties.entrySet().iterator(); itr.hasNext(); ) {
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

    private static String sanitize(Object value) {
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

    private static boolean reportErrors(String prefix, List<String> warnings, List<String> errors) {
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
}
