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
import aQute.bnd.osgi.Packages;
import aQute.service.reporter.Reporter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the {@link ImportedPackageRangeFixer}.
 */
public class ImportedPackageRangeFixerTest {

    private static final String GUAVA_VERSION = "14.0.0";


    @Test
    public void testEmpty() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(Collections.<String, String>emptyMap());

        Analyzer analyzer = new Analyzer();
        fixer.analyzeJar(analyzer);
    }

    @Test
    public void testWithoutReferred() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(Collections.<String, String>emptyMap());

        Analyzer analyzer = new Analyzer();
        analyzer.setClasspath(new Jar[] {
                new Jar("foo")
        });
        fixer.analyzeJar(analyzer);
    }

    @Test
    public void testWithReferredWithoutClasspath() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(Collections.<String, String>emptyMap());

        Analyzer analyzer = mock(Analyzer.class);
        final Packages packages = new Packages();
        Descriptors descriptors = new Descriptors();
        packages.put(descriptors.getPackageRef("com.acme"));
        when(analyzer.getReferred()).thenReturn(packages);
        fixer.analyzeJar(analyzer);
        final Map.Entry<Descriptors.PackageRef, Attrs> actual = foundByPackageName(analyzer, "com.acme");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();
    }

    @Test
    public void testWithReferredMatchingButNotInClasspath() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(Collections.<String, String>emptyMap());

        Analyzer analyzer = mock(Analyzer.class);
        final Packages packages = new Packages();
        Descriptors descriptors = new Descriptors();
        packages.put(descriptors.getPackageRef("com.acme"));
        packages.put(descriptors.getPackageRef("com.google.common.collections"));
        when(analyzer.getReferred()).thenReturn(packages);

        fixer.analyzeJar(analyzer);

        Map.Entry<Descriptors.PackageRef, Attrs> actual = foundByPackageName(analyzer, "com.acme");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();
        actual = foundByPackageName(analyzer, "com.google.common.collections");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();
    }

    @Test
    public void testWithReferredMatchingAndInClasspath() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(Collections.<String, String>emptyMap());

        Analyzer analyzer = mock(Analyzer.class);
        final Packages packages = new Packages();
        Descriptors descriptors = new Descriptors();
        packages.put(descriptors.getPackageRef("com.acme"));
        packages.put(descriptors.getPackageRef("com.google.common.collections"));
        packages.put(descriptors.getPackageRef("org.joda.time"));
        when(analyzer.getReferred()).thenReturn(packages);

        when(analyzer.getClasspath()).thenReturn(ImmutableList.of(createJarFromClasspath("guava"),
                createJarFromClasspath("joda-time")));

        fixer.analyzeJar(analyzer);


        // First package, not in classpath
        Map.Entry<Descriptors.PackageRef, Attrs> actual = foundByPackageName(analyzer, "com.acme");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();

        // Second package in classpath and range fixed by internal file
        actual = foundByPackageName(analyzer, "com.google.common.collections");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isNotNull().contains(GUAVA_VERSION).doesNotContain(")");

        // Third package in classpath, range not fixed
        actual = foundByPackageName(analyzer, "org.joda.time");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();
    }

    @Test
    public void testWithExternalFile() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(ImmutableMap.of("file", new File("src/test/resources/versions.properties").getAbsolutePath()));

        Analyzer analyzer = mock(Analyzer.class);
        final Packages packages = new Packages();
        Descriptors descriptors = new Descriptors();
        packages.put(descriptors.getPackageRef("com.acme"));
        packages.put(descriptors.getPackageRef("com.google.common.collections"));
        packages.put(descriptors.getPackageRef("org.joda.time"));
        packages.put(descriptors.getPackageRef("org.apache.felix.ipojo.manipulator"));
        packages.put(descriptors.getPackageRef("org.mockito"));
        when(analyzer.getReferred()).thenReturn(packages);

        when(analyzer.getClasspath()).thenReturn(ImmutableList.of(
                createJarFromClasspath("guava"),
                createJarFromClasspath("joda-time"),
                createJarFromClasspath("org.apache.felix.ipojo.manipulator"),
                createJarFromClasspath("mockito")));

        fixer.analyzeJar(analyzer);


        // First package, not in classpath
        Map.Entry<Descriptors.PackageRef, Attrs> actual = foundByPackageName(analyzer, "com.acme");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();

        // Second package in classpath and range fixed by internal file
        actual = foundByPackageName(analyzer, "com.google.common.collections");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isNotNull().contains(GUAVA_VERSION).doesNotContain(")");

        // Third package in classpath, set explicitly by external file (no upper bound)
        actual = foundByPackageName(analyzer, "org.joda.time");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).doesNotContain("[").doesNotContain(")");

        // Fourth package in classpath, range set explicitly by external file
        actual = foundByPackageName(analyzer, "org.apache.felix.ipojo.manipulator");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isEqualToIgnoringCase("[1.12, 3)");

        // Fifth package in classpath, customize from external file, no upper bound.
        actual = foundByPackageName(analyzer, "org.mockito");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).doesNotContain("[").doesNotContain(")");
    }

    @Test
    public void testWithMissingExternalFile() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(ImmutableMap.of("file", new File("src/test/resources/missing.properties")
                .getAbsolutePath()));

        Analyzer analyzer = mock(Analyzer.class);
        final Packages packages = new Packages();
        Descriptors descriptors = new Descriptors();
        packages.put(descriptors.getPackageRef("com.acme"));
        packages.put(descriptors.getPackageRef("com.google.common.collections"));
        packages.put(descriptors.getPackageRef("org.joda.time"));
        packages.put(descriptors.getPackageRef("org.apache.felix.ipojo.manipulator"));
        packages.put(descriptors.getPackageRef("org.mockito"));
        when(analyzer.getReferred()).thenReturn(packages);

        when(analyzer.getClasspath()).thenReturn(ImmutableList.of(
                createJarFromClasspath("guava"),
                createJarFromClasspath("joda-time"),
                createJarFromClasspath("org.apache.felix.ipojo.manipulator"),
                createJarFromClasspath("mockito")));

        fixer.analyzeJar(analyzer);


        // First package, not in classpath
        Map.Entry<Descriptors.PackageRef, Attrs> actual = foundByPackageName(analyzer, "com.acme");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();

        // Second package in classpath and range fixed by internal file
        actual = foundByPackageName(analyzer, "com.google.common.collections");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isNotNull().contains(GUAVA_VERSION).doesNotContain(")");

        // Third package in classpath, but no external file
        actual = foundByPackageName(analyzer, "org.joda.time");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isNull();

        // Fourth package in classpath,  but no external file
        actual = foundByPackageName(analyzer, "org.apache.felix.ipojo.manipulator");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isNull();

        // Fifth package in classpath,  but no external file
        actual = foundByPackageName(analyzer, "org.mockito");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isNull();
    }

    @Test
    public void testWhenPrefixSortingIsRequired() throws Exception {
        ImportedPackageRangeFixer fixer = new ImportedPackageRangeFixer();
        Reporter reporter = mock(Reporter.class);
        fixer.setReporter(reporter);
        fixer.setProperties(ImmutableMap.of("file", new File("src/test/resources/sorting.properties").getAbsolutePath
                ()));

        Analyzer analyzer = mock(Analyzer.class);
        final Packages packages = new Packages();
        Descriptors descriptors = new Descriptors();
        packages.put(descriptors.getPackageRef("com.acme"));
        packages.put(descriptors.getPackageRef("com.google.common.collections"));
        packages.put(descriptors.getPackageRef("com.google.common.reflect"));
        packages.put(descriptors.getPackageRef("org.apache.felix.ipojo.manipulator"));
        packages.put(descriptors.getPackageRef("org.apache.felix.ipojo.metadata"));
        packages.put(descriptors.getPackageRef("org.apache.felix.ipojo.handlers"));
        when(analyzer.getReferred()).thenReturn(packages);

        when(analyzer.getClasspath()).thenReturn(ImmutableList.of(
                createJarFromClasspath("guava"),
                createJarFromClasspath("org.apache.felix.ipojo.manipulator"),
                createJarFromClasspath("org.apache.felix.ipojo.metadata")));

        fixer.analyzeJar(analyzer);


        // First package, not in classpath
        Map.Entry<Descriptors.PackageRef, Attrs> actual = foundByPackageName(analyzer, "com.acme");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue()).isEmpty();

        // Second package in classpath and range fixed by internal file but overridden by external file
        actual = foundByPackageName(analyzer, "com.google.common.collections");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isEqualToIgnoringCase("14.0.0");

        // Third package in classpath and range fixed by internal file
        actual = foundByPackageName(analyzer, "com.google.common.reflect");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).doesNotContain("[").doesNotContain(")").contains(GUAVA_VERSION);

        // Fourth package in classpath, range set explicitly by external file
        actual = foundByPackageName(analyzer, "org.apache.felix.ipojo.manipulator");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isEqualToIgnoringCase("[1.12, 2)");

        // Fifth package in classpath,  range set explicitly by external file
        actual = foundByPackageName(analyzer, "org.apache.felix.ipojo.metadata");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isEqualToIgnoringCase("1.0.0");

        actual = foundByPackageName(analyzer, "org.apache.felix.ipojo.handlers");
        assertThat(actual).isNotNull();
        assertThat(actual.getValue().get("version")).isEqualToIgnoringCase("1.12.1");
    }

    private Jar createJarFromClasspath(String name) throws IOException {
        List<URL> list = new ArrayList<>();
        if (this.getClass().getClassLoader() instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) this.getClass().getClassLoader()).getURLs();
            for (URL url : urls) {
                list.add(url);
                if (url.toExternalForm().contains(name)) {
                    return new Jar(url.getFile(), url.getPath());
                }
            }
        }

        throw new IllegalArgumentException("Cannot find " + name + " in classpath - " + list);
    }

    private Map.Entry<Descriptors.PackageRef, Attrs> foundByPackageName(Analyzer analyzer, String name) {
        for (Map.Entry<Descriptors.PackageRef, Attrs> entry : analyzer.getReferred().entrySet()) {
            if (name.equals(entry.getKey().getFQN())) {
                return entry;
            }
        }
        throw new IllegalArgumentException("Cannot find " + name + " in referred packages");
    }
}