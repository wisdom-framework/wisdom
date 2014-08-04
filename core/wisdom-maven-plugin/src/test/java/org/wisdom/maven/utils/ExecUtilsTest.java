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

import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class ExecUtilsTest {

    Map<String, String> saved = new HashMap<>();


    public ExecUtilsTest saveAndSet(String key, String value) {
        saved.put(key, System.getProperty(key));
        System.setProperty(key, value);
        return this;
    }

    @Test
    public void dump() {
        String osname = System.getProperty("os.name");
        String osarch = System.getProperty("os.arch");
        String datamodel = System.getProperty("sun.arch.data.model");

        System.out.println("Test executed on '" + osname + "' on a " + osarch + " CPU with a " + datamodel + " " +
                "bits format");
    }

    @After
    public void restore() {
        for (Map.Entry<String, String> entry : saved.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
        saved.clear();
    }

    @Test
    public void testIsWindows() throws Exception {
        saveAndSet("os.name", "Windows 95");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows 98");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows Me");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows NT");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows 2000");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows XP");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows 2003");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows 8");
        assertThat(ExecUtils.isWindows()).isTrue();
        saveAndSet("os.name", "Windows 8.1");
        assertThat(ExecUtils.isWindows()).isTrue();

        saveAndSet("os.name", "Linux");
        assertThat(ExecUtils.isWindows()).isFalse();
    }

    @Test
    public void testIsMac() throws Exception {
        saveAndSet("os.name", "Windows 95");
        assertThat(ExecUtils.isMac()).isFalse();

        saveAndSet("os.name", "Mac OS");
        assertThat(ExecUtils.isMac()).isTrue();

        saveAndSet("os.name", "Mac OS X");
        assertThat(ExecUtils.isMac()).isTrue();
    }

    @Test
    public void testIsLinux() throws Exception {
        saveAndSet("os.name", "Microsoft Windows 3.1");
        assertThat(ExecUtils.isLinux()).isFalse();

        saveAndSet("os.name", "Linux");
        assertThat(ExecUtils.isLinux()).isTrue();

        saveAndSet("os.name", "AIX");
        assertThat(ExecUtils.isLinux()).isTrue();

        saveAndSet("os.name", "Digital Unix");
        assertThat(ExecUtils.isLinux()).isTrue();
    }

    @Test
    public void testIs64bit() throws Exception {
        // First test - clear os arch and check that the data format is 64 bits
        saveAndSet("os.arch", "");
        saveAndSet("sun.arch.data.model", "64");
        assertThat(ExecUtils.is64bit()).isTrue();

        restore();

        // Second test, change data format to 32
        saveAndSet("os.arch", "");
        saveAndSet("sun.arch.data.model", "32");
        assertThat(ExecUtils.is64bit()).isFalse();

        restore();

        // Third test, clear the data model, and set the arch to x86_64
        saveAndSet("os.arch", "x86_64");
        saveAndSet("sun.arch.data.model", "");
        assertThat(ExecUtils.is64bit()).isTrue();

        restore();

        // Fourth test, clear the data model, and set the arch to x86
        saveAndSet("os.arch", "x86");
        saveAndSet("sun.arch.data.model", "");
        assertThat(ExecUtils.is64bit()).isFalse();

    }

    @Test
    public void testIsARM() throws Exception {
        // Test 1) x86
        saveAndSet("os.arch", "x86");
        assertThat(ExecUtils.isARM()).isFalse();

        restore();

        // Test 2) x86_64
        saveAndSet("os.arch", "x86_64");
        assertThat(ExecUtils.isARM()).isFalse();

        restore();

        // Test 3) ARM
        saveAndSet("os.arch", "ARM");
        assertThat(ExecUtils.isARM()).isTrue();
    }

    @Test
    public void findExecutable() {
        if (ExecUtils.isWindows()) {
            assertThat(ExecUtils.find("dir")).isFile();
            assertThat(ExecUtils.findExecutableInSystemPath("dir")).isFile();
        } else {
            assertThat(ExecUtils.find("ls")).isFile();
            assertThat(ExecUtils.findExecutableInSystemPath("ls")).isFile();
        }
    }
}