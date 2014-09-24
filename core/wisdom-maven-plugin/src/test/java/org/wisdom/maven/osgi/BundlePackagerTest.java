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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wisdom.maven.osgi.BundlePackager;
import org.wisdom.maven.osgi.Packages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check bundle packager.
 */
public class BundlePackagerTest {

        private File basedir = new File("taget/junk");

        @Test
        public void testDefaultsOnEmptyProjects() throws IOException {
            ProjectScanner scanner = mock(ProjectScanner.class);
            when(scanner.getLocalResources(anyBoolean())).thenReturn(Collections.<String>emptySet());
            when(scanner.getPackagesFromMainSources()).thenReturn(Collections.<String>emptySet());
            Properties instructions = BundlePackager.populatePropertiesWithDefaults(basedir, new Properties(), scanner);
            assertThat(instructions).hasSize(1).containsEntry("Import-Package", "*");
        }

    /**
     * Reproduces the hibernate validator configuration
     * @throws IOException should not happen (fail the test if so).
     */
    @Test
    public void testDefaultInstructionsMerge() throws IOException {
        ProjectScanner scanner = mock(ProjectScanner.class);
        when(scanner.getLocalResources(anyBoolean())).thenReturn(Collections.<String>emptySet());
        when(scanner.getPackagesFromMainSources()).thenReturn(ImmutableSet.of("org.wisdom.validation.hibernate"));

        Properties fromBND = new Properties();
        File hibernate = new File("src/test/resources/instructions/hibernate.bnd");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(hibernate);
            fromBND.load(fis);
        } finally {
            IOUtils.closeQuietly(fis);
        }

        Properties instructions = BundlePackager.populatePropertiesWithDefaults(basedir, fromBND, scanner);
        assertThat(instructions).hasSize(3).containsKeys("Import-Package", "Private-Package", "Export-Package");
        assertThat(instructions.getProperty("Private-Package"))
                .contains("org.wisdom.validation.hibernate;-split-package:=merge-first")
                .contains("org.hibernate.validator*,")
                .contains(" javax.el*");
        assertThat(instructions.getProperty("Export-Package")).isEqualTo("org.hibernate.validator.constraints*");
        assertThat(instructions.getProperty("Import-Package")).contains("org.jboss.logmanager*;resolution:=optional");
    }




}
