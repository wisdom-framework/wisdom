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

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.manipulator.Pojoization;

import aQute.bnd.osgi.Builder;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Packages the bundle using BND.
 */
public class BundlePackager implements org.wisdom.maven.Constants {

    private BundlePackager(){
    	//Hide default constructor
    }

    public static void bundle(File basedir, File output) throws Exception {
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
        Builder builder = getOSGiBuilder(basedir, properties, computeClassPath(basedir));
        builder.build();

        reportErrors("BND ~> ", builder.getWarnings(), builder.getErrors());
        File bnd = File.createTempFile("bnd-", ".jar");
        File ipojo = File.createTempFile("ipojo-", ".jar");
        builder.getJar().write(bnd);

        Pojoization pojoization = new Pojoization();
        pojoization.pojoization(bnd, ipojo, new File(basedir, "src/main/resources"));
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
        File osgi = new File(baseDir, "target/osgi/osgi.properties");
        merge(properties, osgi);
    }

    private static void populatePropertiesWithDefaults(File basedir, Properties properties) throws IOException {
        List<String> privates = new ArrayList<>();
        List<String> exports = new ArrayList<>();

        File classes = new File(basedir, "target/classes");

        Set<String> packages = new LinkedHashSet<>();
        if (classes.isDirectory()) {
            Jar jar = new Jar("", classes);
            packages.addAll(jar.getPackages());
            jar.close();
        }

        for (String s : packages) {
            if (shouldBeExported(s)) {
                exports.add(s);
            } else {
                if (!s.isEmpty()) {
                    privates.add(s + ";-split-package:=merge-first");
                }
            }
        }

        properties.put(Constants.PRIVATE_PACKAGE, toClause(privates));
        if (!exports.isEmpty()) {
            properties.put(Constants.EXPORT_PACKAGE, toClause(exports));
        }
    }

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
        return !packageName.isEmpty() && (service || api);
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
