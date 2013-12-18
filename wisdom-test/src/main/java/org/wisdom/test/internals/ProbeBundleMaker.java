package org.wisdom.test.internals;

import aQute.bnd.osgi.Builder;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Processor;
import com.google.common.reflect.ClassPath;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.wisdom.test.probe.Activator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Class responsible for creating the probe bundle.
 * The probe bundle contains both application files and the test files.
 * Such choice is made to avoid classloading issues when accessing controllers.
 */
public class ProbeBundleMaker {

    public static final String INSTRUCTIONS_FILE = "src/main/osgi/osgi.bnd";
    public static final String BUNDLE_NAME = "wisdom-probe-bundle";
    public static final String PACKAGES_TO_ADD = "org.wisdom.test.parents.*, " +
            "org.wisdom.test.probe";

    public static InputStream probe() throws Exception {
        Properties properties = readInstructionsFromBndFiles();
        enhancedInstructionsForProbe(properties);
        Builder builder = getOSGiBuilder(properties, computeClassPath());
        buildOSGiBundle(builder);
        reportErrors("BND ~> ", builder.getWarnings(), builder.getErrors());
        File bnd = File.createTempFile("bnd-", ".jar");
        File ipojo = File.createTempFile("ipojo-", ".jar");
        //File ipojo = new File("ipojo-application.jar");
        builder.getJar().write(bnd);

        Pojoization pojoization = new Pojoization();
        pojoization.pojoization(bnd, ipojo, new File("src/main/resources"));
        reportErrors("iPOJO ~> ", pojoization.getWarnings(), pojoization.getErrors());

        return new FileInputStream(ipojo);
    }

    private static void enhancedInstructionsForProbe(Properties properties) throws IOException {
        if (properties.isEmpty()) {
            populatePropertiesWithDefaults(properties);
        }

        // We must add the probe packages.
        String privates = properties.getProperty("Private-Package");
        if (privates == null) {
            properties.put("Private-Package", PACKAGES_TO_ADD);
        } else {
            privates = privates + ", " + PACKAGES_TO_ADD;
            properties.put("Private-Package", privates);
        }

        //TODO Check we don't have an activator already.
        properties.put(Constants.BUNDLE_ACTIVATOR, Activator.class.getName());
    }

    private static void populatePropertiesWithDefaults(Properties properties) throws IOException {
        List<String> privates = new ArrayList<>();
        List<String> exports = new ArrayList<>();

        File classes = new File("target/classes");
        File tests = new File("target/test-classes");

        Set<String> packages = new LinkedHashSet<>();
        if (classes.isDirectory()) {
            Jar jar = new Jar(".", classes);
            packages.addAll(jar.getPackages());
        }

        if (tests.isDirectory()) {
            Jar jar = new Jar(".", tests);
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
        properties.put(Constants.IMPORT_PACKAGE, "*");
        properties.put(Constants.BUNDLE_SYMBOLIC_NAME_ATTRIBUTE, BUNDLE_NAME);
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
        File classes = new File("target/classes");
        File tests = new File("target/test-classes");

        if (classes.isDirectory()) {
            list.add(new Jar(".", classes));
        }

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
        synchronized (ProbeBundleMaker.class) // protect setBase...getBndLastModified which uses static DateFormat
        {
            builder.setBase(new File(""));
        }
        builder.setProperties(sanitize(properties));
        if (classpath != null) {
            builder.setClasspath(classpath);
        }

        return builder;
    }

    private static Properties readInstructionsFromBndFiles() throws IOException {
        Properties properties = new Properties();
        File instructionFile = new File(INSTRUCTIONS_FILE);
        if (instructionFile.isFile()) {
            InputStream is = null;
            try {
                is = new FileInputStream(instructionFile);
                properties.load(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return properties;
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

    private static class JarFromClassloader extends Jar {
        public JarFromClassloader(ClassPath classpath) {
            super("classrealms");
            ClassPathResource.build(this, classpath, null);
        }
    }
}
