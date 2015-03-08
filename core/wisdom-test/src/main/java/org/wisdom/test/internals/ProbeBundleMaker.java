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

import aQute.bnd.osgi.*;
import com.google.common.reflect.ClassPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.apache.felix.ipojo.manipulator.util.IsolatedClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.maven.osgi.BundlePackager;
import org.wisdom.maven.osgi.ProjectScanner;
import org.wisdom.test.probe.Activator;
import org.wisdom.test.shared.InVivoRunnerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Class responsible for creating the probe bundle.
 * The probe does not include the application bundle, only the test classes and some additional resources.
 * Application class are accessed using a custom classloader.
 */
public class ProbeBundleMaker {

    /**
     * The name of the probe bundle.
     */
    public static final String BUNDLE_NAME = "wisdom-probe-bundle";

    /**
     * The packages to add in the probe bundle.
     * Helpers need to be integrated to the probe bundle to avoid class loading issue (#446)
     */
    public static final String PACKAGES_TO_ADD = "org.wisdom.test.parents.*, " +
            "org.wisdom.test.probe, org.wisdom.test.assertions, " +
            "org.ow2.chameleon.testing.helpers," +
            "org.ow2.chameleon.testing.helpers.constants";

    /**
     * The path of the probe bundle file.
     */
    public static final String PROBE_FILE = "target/osgi/probe.jar";

    /**
     * The test classes path.
     */
    public static final String TEST_CLASSES = "target/test-classes";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProbeBundleMaker.class);

    static {
        // At initialization, delete the probe bundle if exist
        File probe = new File(PROBE_FILE);
        if (probe.isFile()) {
            FileUtils.deleteQuietly(probe);
        }
    }

    private ProbeBundleMaker() {
        //Unused
    }

    /**
     * Creates the test probe. The test probe is a bundle containing all the classes from `src/test/java` and
     * resources from `src/test/resources`. The bundle computes the imports automatically, but all are optionals. The
     * created bundle has a specific bundle activator exposing the {@link InVivoRunnerFactory} service. The created
     * bundle has the following symbolic name: "Wisdom-Test-Probe"
     *
     * @return an input stream on the bundle
     * @throws Exception if the probe creation fails
     */
    public static InputStream probe() throws Exception {   //NOSONAR we throw exception as BND throws Exception.
        File probe = new File(PROBE_FILE);
        if (probe.isFile()) {
            return new FileInputStream(probe);
        }

        Properties maven = BundlePackager.readMavenProperties(new File("."));

        Properties instructions = new Properties();
        getProbeInstructions(instructions, maven);
        Builder builder = getOSGiBuilder(instructions, computeClassPath());
        builder.build();
        reportErrors("BND ~> ", builder.getWarnings(), builder.getErrors());
        File bnd = File.createTempFile("probe", ".jar");
        builder.getJar().write(bnd);

        // No need for a privilege block here, we are in Maven - no security involved.
        IsolatedClassLoader classLoader = new IsolatedClassLoader(ProbeBundleMaker.class.getClassLoader(), //NOSONAR
                true);
        File tests = new File(TEST_CLASSES);
        classLoader.addURL(tests.toURI().toURL());
        Pojoization pojoization = new Pojoization();
        pojoization.pojoization(bnd, probe, new File("src/test/resources"), classLoader);
        reportErrors("iPOJO ~> ", pojoization.getWarnings(), pojoization.getErrors());

        return new FileInputStream(probe);
    }

    private static void getProbeInstructions(Properties instructions, Properties maven) throws IOException {
        List<String> privates = new ArrayList<>();
        List<String> exports = new ArrayList<>();

        final File basedir = new File(maven.getProperty("project.baseDir"));
        ProjectScanner scanner = new ProjectScanner(basedir);

        // Do local resources
        String resources = BundlePackager.getLocalResources(basedir, true, scanner);
        if (!resources.isEmpty()) {
            instructions.put(Analyzer.INCLUDE_RESOURCE, resources);
        }

        Set<String> packages = scanner.getPackagesFromTestSources();

        for (String s : packages) {
            if (s.endsWith("service") || s.endsWith("services")) {
                exports.add(s);
            } else {
                if (!s.isEmpty()) {
                    privates.add(s + ";-split-package:=first");
                }
            }
        }

        instructions.put(Constants.PRIVATE_PACKAGE, toClause(privates) + "," + PACKAGES_TO_ADD);
        if (!exports.isEmpty()) {
            instructions.put(Constants.EXPORT_PACKAGE, toClause(privates));
        }
        instructions.put(Constants.IMPORT_PACKAGE, "org.osgi.framework;version=1.7, *;resolution:=optional");
        instructions.put(Constants.BUNDLE_SYMBOLIC_NAME_ATTRIBUTE, BUNDLE_NAME);

        instructions.put(Constants.BUNDLE_ACTIVATOR, Activator.class.getName());

        // For debugging purpose, dump the instructions to target/osgi/default-instructions.instructions
        FileOutputStream fos = null;
        try {
            File out = new File("target/osgi/probe-instructions.properties");
            fos = new FileOutputStream(out);
            instructions.store(fos, "BND Instructions for test probe");
        } catch (IOException e) { // NOSONAR
            // Ignore it.
        } finally {
            IOUtils.closeQuietly(fos);
        }
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
                                            Jar[] classpath) throws Exception {   //NOSONAR rethrows exceptions from BND
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
                LOGGER.error(prefix + " Duplicate path '" + duplicate + "' in Include-Resource");
            } else {
                LOGGER.error(prefix + " : " + msg);
                hasErrors = true;
            }
        }
        return hasErrors;
    }

    /**
     * Makes the given classpath looks like a Jar.
     */
    private static class JarFromClassloader extends Jar {
        /**
         * Creates an instance of {@link org.wisdom.test.internals.ProbeBundleMaker.JarFromClassloader}
         * @param classpath the classpath
         */
        public JarFromClassloader(ClassPath classpath) {
            super("classrealms");
            ClassPathResource.build(this, classpath, null);
        }
    }
}
