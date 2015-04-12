/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.bnd.plugins;

import aQute.bnd.header.Attrs;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Descriptors;
import aQute.bnd.osgi.Jar;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.service.reporter.Reporter;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A BND plugin checking if one of the import packages is matching a list of known package and fix the imported
 * versions. This plugin avoid generating ranges for dependencies that are forward compatible and when their major
 * version is bumped.
 * <p>
 * The plugin loads its data from an internal file and also look for a 'src/main/osgi/versions.properties' file in
 * the project, so each project can configure the versions.
 * <p>
 * Files are properties file, where values are optional (this is still a valid properties file). For instance:
 * <pre>
 * {@code
 * com.google.common*
 * com.acme*: 1.0.0
 * com.foo: [1.0.0, 2)
 * }
 * </pre>
 * <p>
 * In this file, the `com.google.common` packages see their imported versions fixed to the "[xxx,)", where "xxx" is
 * the (OSGi-compliant) version of the dependency providing the package. Notice that there is no upper bound. The
 * `com.acme` packages see their imported version set to 1.0.0, while `com.foo` packages are imported using the
 * specified range.
 * <p>
 * These fixes can be done in the `osgi.bnd` version, but 1) it's automatic for Guava, 2) let you have a shared file.
 */
public class ImportedPackageRangeFixer implements AnalyzerPlugin, Plugin {

    /**
     * The name of the property that indicate the version file if any.
     */
    public static final String RANGE_FILE = "file";

    /**
     * The internal version file.
     */
    public static final String INTERNAL_RANGE_FILE_URL = "ranges/versions.properties";

    /**
     * The default path to find the version file.
     */
    public static final String DEFAULT_RANGE_FILE = "src/main/osgi/versions.properties";

    private Map<String, String> configuration;
    private Reporter reporter;

    private Set<Range> ranges = new TreeSet<>();

    /**
     * Analyzes the jar and update the version range.
     *
     * @param analyzer the analyzer
     * @return {@code false}
     * @throws Exception if the analaysis fails.
     */
    @Override
    public boolean analyzeJar(Analyzer analyzer) throws Exception {
        loadInternalRangeFix();
        loadExternalRangeFix();

        if (analyzer.getReferred() == null) {
            return false;
        }

        // Data loaded, start analysis
        for (Map.Entry<Descriptors.PackageRef, Attrs> entry : analyzer.getReferred().entrySet()) {
            for (Range range : ranges) {
                if (range.matches(entry.getKey().getFQN())) {
                    String value = range.getRange(analyzer);
                    if (value != null) {
                        reporter.warning("Updating import version of " + range.name + " to " + value);
                        entry.getValue().put("version", value);
                    }
                }
            }
        }
        return false;
    }

    private void loadExternalRangeFix() throws IOException {
        if (configuration == null) {
            return;
        }

        String file = configuration.get(RANGE_FILE);
        if (file == null) {
            file = DEFAULT_RANGE_FILE;
        }

        File theFile = new File(file);
        if (theFile.isFile()) {
            addToRanges(load(theFile));
        }
    }

    private void loadInternalRangeFix() throws IOException {
        URL url = this.getClass().getClassLoader().getResource(INTERNAL_RANGE_FILE_URL);
        Preconditions.checkNotNull(url);
        Properties loaded = load(url);
        addToRanges(loaded);
    }

    private void addToRanges(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            ranges.add(new Range(key, value));
        }
    }

    /**
     * Callbacks called by BND with the properties.
     *
     * @param map the properties
     */
    @Override
    public void setProperties(Map<String, String> map) {
        this.configuration = map;
    }

    /**
     * Callbacks called by BND with the logger.
     *
     * @param reporter the logger
     */
    @Override
    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }


    /**
     * Utility method to load a properties file.
     *
     * @param file the file
     * @return the read properties, empty if the file cannot be found.
     * @throws IOException if the file cannot be loaded.
     */
    public static Properties load(File file) throws IOException {
        if (file.isFile()) {
            return load(file.toURI().toURL());
        }
        return new Properties();
    }

    /**
     * Utility method to load a properties file pointed by the given url.
     *
     * @param url the url
     * @return the read properties, empty if the file cannot be found.
     * @throws IOException if the file cannot be loaded.
     */
    public static Properties load(URL url) throws IOException {
        InputStream fis = null;
        try {
            Properties props = new Properties();
            fis = url.openStream();
            props.load(fis);
            return props;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private class Range implements Comparable<Range> {
        final String name;
        final String value;
        final Pattern regex;

        /**
         * Field acting as a cache storing the version of the jar providing the package. This field is only used if
         * we have no value.
         */
        private String foundRange;

        private Range(String name, String value) {
            this.name = name;
            this.value = value;
            this.regex = Pattern.compile(name.trim().replace(".", "\\.").replace("*", ".*"));
        }

        private boolean matches(String pck) {
            return regex.matcher(pck).matches();
        }


        private String getRange(Analyzer analyzer) throws Exception {
            if (foundRange != null) {
                return foundRange;
            }
            if (Strings.isNullOrEmpty(value)) {
                for (Jar jar : analyzer.getClasspath()) {
                    if (isProvidedByJar(jar) && jar.getVersion() != null) {
                        foundRange = jar.getVersion();
                        return jar.getVersion();
                    }
                }
                // Cannot find a provider.
                reporter.error("Cannot find a dependency providing " + name + " in the classpath");
                return null;
            } else {
                return value;
            }
        }

        private boolean isProvidedByJar(Jar jar) {
            for (String s : jar.getPackages()) {
                if (matches(s)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Method used to sort range. Longest prefix first.
         *
         * @param o the other range
         * @return 1 if the current range is longer than the given range.
         */
        @Override
        public int compareTo(Range o) {
            return Integer.compare(this.regex.pattern().length(), o.regex.pattern().length());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Range range = (Range) o;
            return Objects.equal(name, range.name) &&
                    Objects.equal(value, range.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, value);
        }
    }
}
