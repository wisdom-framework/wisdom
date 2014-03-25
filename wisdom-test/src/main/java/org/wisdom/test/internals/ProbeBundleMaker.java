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
package org.wisdom.test.internals;

import aQute.bnd.osgi.Builder;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Processor;

import com.google.common.reflect.ClassPath;

import org.apache.commons.io.FileUtils;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.test.WisdomBlackBoxRunner;
import org.wisdom.test.probe.Activator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Class responsible for creating the probe bundle.
 * The probe does not include the application bundle, only the test classes and some additional resources.
 * Application class are accessed using a custom classloader.
 */
public class ProbeBundleMaker {

    public static final String BUNDLE_NAME = "wisdom-probe-bundle";

    public static final String PACKAGES_TO_ADD = "org.wisdom.test.parents.*, " +
            "org.wisdom.test.probe";
    public static final String PROBE_FILE = "target/osgi/probe.jar";
    
    public static final String TEST_CLASSES = "target/test-classes";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProbeBundleMaker.class);

    static {
        // At initialization, delete the probe bundle if exist
        File probe = new File(PROBE_FILE);
        if (probe.isFile()) {
            FileUtils.deleteQuietly(probe);
        }
    }

    private ProbeBundleMaker(){
        //Unused
    }

    public static InputStream probe() throws Exception {
        File probe = new File(PROBE_FILE);
        if (probe.isFile()) {
            return new FileInputStream(probe);
        }

        Properties properties = new Properties();
        getProbeInstructions(properties);
        Builder builder = getOSGiBuilder(properties, computeClassPath());
        builder.build();
        reportErrors("BND ~> ", builder.getWarnings(), builder.getErrors());
        File bnd = File.createTempFile("probe", ".jar");
        builder.getJar().write(bnd);

        Pojoization pojoization = new Pojoization();
        pojoization.pojoization(bnd, probe, new File("src/test/resources"));
        reportErrors("iPOJO ~> ", pojoization.getWarnings(), pojoization.getErrors());

        return new FileInputStream(probe);
    }

    private static void getProbeInstructions(Properties properties) throws IOException {
        List<String> privates = new ArrayList<>();
        List<String> exports = new ArrayList<>();

        File tests = new File(TEST_CLASSES);

        Set<String> packages = new LinkedHashSet<>();
        if (tests.isDirectory()) {
            Jar jar = new Jar(".", tests);
            packages.addAll(jar.getPackages());
            jar.close();
        }

        for (String s : packages) {
            if (s.endsWith("service") || s.endsWith("services")) {
                exports.add(s);
            } else {
                if (!s.isEmpty()) {
                    privates.add(s + ";-split-package:=first");
                }
            }
        }

        properties.put(Constants.PRIVATE_PACKAGE, toClause(privates) + "," + PACKAGES_TO_ADD);
        if (!exports.isEmpty()) {
            properties.put(Constants.EXPORT_PACKAGE, toClause(privates));
        }
        properties.put(Constants.IMPORT_PACKAGE, "*;resolution:=optional");
        properties.put(Constants.BUNDLE_SYMBOLIC_NAME_ATTRIBUTE, BUNDLE_NAME);

        properties.put(Constants.BUNDLE_ACTIVATOR, Activator.class.getName());
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

    private static Jar[] computeClassPath() throws IOException {
        List<Jar> list = new ArrayList<>();
        File tests = new File(TEST_CLASSES);

        if (tests.isDirectory()) {
            list.add(new Jar(".", tests));
        }

        ClassPath classpath = ClassPath.from(ProbeBundleMaker.class.getClassLoader());
        list.add(new JarFromClassloader(classpath));

        Jar[] cp = new Jar[list.size()];
        list.toArray(cp);

        return cp;

    }

    protected static Builder getOSGiBuilder(Properties properties,
            Jar[] classpath) throws Exception {
        Builder builder = new Builder();
        // protect setBase...getBndLastModified which uses static DateFormat
        synchronized (ProbeBundleMaker.class) {
            builder.setBase(new File(""));
        }
        builder.setProperties(sanitize(properties));
        if (classpath != null) {
            builder.setClasspath(classpath);
        }

        return builder;
    }

    protected static Properties sanitize(Properties properties) {
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

    protected static boolean reportErrors(String prefix, List<String> warnings, List<String> errors) {
        for (String msg : warnings) {
            LOGGER.error(prefix + " : " + msg);
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

    private static class JarFromClassloader extends Jar {
        public JarFromClassloader(ClassPath classpath) {
            super("classrealms");
            ClassPathResource.build(this, classpath, null);
        }
    }
}
