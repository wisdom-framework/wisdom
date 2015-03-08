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
package org.wisdom.maven.osgi;

import aQute.bnd.osgi.*;
import aQute.libg.reporter.ReporterAdapter;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.apache.felix.ipojo.manipulator.util.Classpath;
import org.wisdom.bnd.plugins.ImportedPackageRangeFixer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


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
    public static void bundle(File basedir, File output, Reporter reporter) throws IOException {
        ProjectScanner scanner = new ProjectScanner(basedir);

        // Loads the properties inherited from Maven.
        Properties instructions = readMavenProperties(basedir);
        // Loads the properties from the BND file.
        Properties fromBnd = readInstructionsFromBndFile(basedir);
        if (fromBnd == null) {
            // No bnd files, use default instructions
            instructions = populatePropertiesWithDefaults(basedir, instructions, scanner);
        } else {
            // We have a BND file.
            // Do we have to merge ?
            String noDefaultValue = fromBnd.getProperty("-no-default");
            if (!"true".equalsIgnoreCase(noDefaultValue)) {
                // So we need to merge the default with the bnd files
                // 1) merge the instructions from the bnd files with the default
                // 2) merge the resulting set of instruction onto the maven properties
                // (and override the default)
                instructions = Instructions.mergeAndOverrideExisting(
                        instructions,
                        populatePropertiesWithDefaults(basedir, fromBnd, scanner));
            } else {
                instructions = Instructions.mergeAndOverrideExisting(instructions, fromBnd);
            }
        }

        // Manage Embedded dependencies
        DependencyEmbedder ed = new DependencyEmbedder(instructions, reporter);
        instructions = ed.generate(instructions, org.wisdom.maven.osgi.Classpath.load(basedir));

        // Integrate custom headers added by other plugins.
        instructions = mergeExtraHeaders(basedir, instructions);

        // For debugging purpose, dump the instructions to target/osgi/instructions.properties
        FileOutputStream fos = null;
        try {
            File out = new File(basedir, "target/osgi/instructions.properties");
            fos = new FileOutputStream(out);
            instructions.store(fos, "Wisdom BND Instructions");
        } catch (IOException e) { // NOSONAR
            // Ignore it.
        } finally {
            IOUtils.closeQuietly(fos);
        }

        // Instructions loaded, start the build sequence.
        final Jar[] jars = org.wisdom.maven.osgi.Classpath.computeClassPath(basedir);

        File bnd;
        File ipojo;
        Builder builder = null;
        try {
            builder = getOSGiBuilder(basedir, instructions, jars);
            // The next sequence is weird
            // First build the bundle with the given instruction
            // Then analyze to apply the plugin and fix
            // finally, rebuild with the updated metadata
            // Without the first build, embedded dependencies and private packages from classpath are not analyzed.
            builder.build();
            builder.analyze();
            builder.build();

            reportErrors(builder.getWarnings(), builder.getErrors(), reporter);
            bnd = File.createTempFile("bnd-", ".jar");
            ipojo = File.createTempFile("ipojo-", ".jar");
            builder.getJar().write(bnd);
        } catch (Exception e) {
            throw new IOException("Cannot build the OSGi bundle", e);
        }  finally {
            if (builder != null) {
                builder.close();
            }
        }

        final Set<String> elements = org.wisdom.maven.osgi.Classpath.computeClassPathElement(basedir);
        Classpath classpath = new Classpath(elements);
        Pojoization pojoization = new Pojoization();
        pojoization.pojoization(bnd, ipojo, new File(basedir, "src/main/resources"),
                classpath.createClassLoader());
        reportErrors(pojoization.getWarnings(), pojoization.getErrors(), reporter);

        Files.move(Paths.get(ipojo.getPath()), Paths.get(output.getPath()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * If a bundle has added extra headers, they are added to the bundle manifest.
     *
     * @param baseDir    the project directory
     * @param properties the current set of properties in which the read metadata are written
     * @return the merged set of properties
     */
    private static Properties mergeExtraHeaders(File baseDir, Properties properties) throws IOException {
        File extra = new File(baseDir, EXTRA_HEADERS_FILE);
        return Instructions.merge(properties, extra);
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
        props = Instructions.merge(props, extra);
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
     * We should have generated a {@code target/osgi/osgi.properties} file with the metadata we inherit from Maven.
     *
     * @param baseDir the project directory
     * @return the computed set of properties
     */
    public static Properties readMavenProperties(File baseDir) throws IOException {
        return Instructions.load(new File(baseDir, org.wisdom.maven.Constants.OSGI_PROPERTIES));
    }


    /**
     * Populates the given properties object with our BND default instructions (computed for the current project).
     * Entries are not added if the given properties file already contains these values.
     *
     * @param basedir      the project's base directory
     * @param instructions the current set of properties in which the read metadata are written
     * @param scanner      the project scanner to retrieve information about the sources and resources contained in the
     *                     project
     * @return the final set of instructions (containing the default instructions merged into the
     * given set of properties)
     * @throws IOException if something wrong happens
     */
    protected static Properties populatePropertiesWithDefaults(File basedir, Properties instructions,
                                                               ProjectScanner scanner) throws IOException {
        Properties defaultInstructions = new Properties();

        List<String> privates = new ArrayList<>();
        List<String> exports = new ArrayList<>();

        // Do local resources
        String resources = getLocalResources(basedir, false, scanner);
        if (!resources.isEmpty()) {
            defaultInstructions.put(Analyzer.INCLUDE_RESOURCE, resources);
        }

        defaultInstructions.put(Constants.IMPORT_PACKAGE, "*");

        for (String s : scanner.getPackagesFromMainSources()) {
            if (Packages.shouldBeExported(s)) {
                exports.add(s);
            } else {
                if (!s.isEmpty() && !s.equals(".")) {
                    privates.add(s + ";-split-package:=merge-first");
                }
            }
        }

        defaultInstructions.put(Constants.PRIVATE_PACKAGE, Packages.toClause(privates));
        defaultInstructions.put(Constants.EXPORT_PACKAGE, Packages.toClause(exports));

        return Instructions.mergeAndSkipExisting(instructions, defaultInstructions);
    }

    /**
     * Gets local resources.
     *
     * @param basedir the project's base directory
     * @param test    whether or not we compute the test resources
     * @param scanner the project scanner to retrieve information about the sources and resources contained in the
     *                project
     * @return the resource clause
     */
    public static String getLocalResources(File basedir, boolean test, ProjectScanner scanner) {
        final String basePath = basedir.getAbsolutePath();
        String target = "target/classes";
        if (test) {
            target = "target/test-classes";
        }
        Set<String> files = scanner.getLocalResources(test);
        Set<String> pathSet = new LinkedHashSet<>();

        for (String name : files) {
            String path = target + '/' + name;

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
            pathSet.add(path);
        }

        return Joiner.on(", ").join(pathSet);
    }


    private static Builder getOSGiBuilder(File basedir, Properties properties,
                                          Jar[] classpath) {
        Builder builder = new Builder();
        synchronized (BundlePackager.class) {
            builder.setBase(basedir);
        }
        // Add the range fixer plugin
        final ImportedPackageRangeFixer plugin = new ImportedPackageRangeFixer();
        plugin.setReporter(builder);
        plugin.setProperties(Collections.<String, String>emptyMap());
        builder.addBasicPlugin(plugin);

        builder.setProperties(Instructions.sanitize(properties));
        if (classpath != null) {
            builder.setClasspath(classpath);
        }
        return builder;
    }

    private static Properties readInstructionsFromBndFile(File basedir) throws IOException {
        File instructionFile = new File(basedir, INSTRUCTIONS_FILE);
        if (!instructionFile.isFile()) {
            return null;
        }
        return Instructions.load(instructionFile);
    }


    private static boolean reportErrors(List<String> warnings, List<String> errors, Reporter reporter) {
        for (String msg : warnings) {
            reporter.warn(msg);
        }

        boolean hasErrors = false;
        String fileNotFound = "Input file does not exist: ";
        for (String msg : errors) {
            if (msg.startsWith(fileNotFound) && msg.endsWith("~")) {
                // treat as warning; this error happens when you have duplicate entries in Include-Resource
                String duplicate = Processor.removeDuplicateMarker(msg.substring(fileNotFound.length()));
                reporter.warn("Duplicate path '" + duplicate + "' in Include-Resource");
            } else {
                reporter.error(msg);
                hasErrors = true;
            }
        }
        return hasErrors;
    }

}
