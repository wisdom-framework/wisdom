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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ExecUtilsTest {

    @Test
    public void dump() {
        String osname = System.getProperty("os.name");
        String osarch = System.getProperty("os.arch");
        String datamodel = System.getProperty("sun.arch.data.model");

        System.out.println("Test executed on '" + osname + "' on a " + osarch + " CPU with a " + datamodel + " " +
                "bits format");

        // Check against NPE.
        ExecUtils.isWindows();
        ExecUtils.isLinux();
        ExecUtils.isMac();
        ExecUtils.is64bits();
        ExecUtils.isARM();
    }


    @Test
    public void testIsWindows() throws Exception {
        assertThat(ExecUtils.isWindows("Windows 95")).isTrue();
        assertThat(ExecUtils.isWindows("Windows 98")).isTrue();
        assertThat(ExecUtils.isWindows("Windows Me")).isTrue();
        assertThat(ExecUtils.isWindows("Windows NT")).isTrue();
        assertThat(ExecUtils.isWindows("Windows 2000")).isTrue();
        assertThat(ExecUtils.isWindows("Windows XP")).isTrue();
        assertThat(ExecUtils.isWindows("Windows 2003")).isTrue();
        assertThat(ExecUtils.isWindows("Windows 8")).isTrue();
        assertThat(ExecUtils.isWindows("Windows 8.1")).isTrue();

        assertThat(ExecUtils.isWindows("Linux")).isFalse();
    }

    @Test
    public void testIsMac() throws Exception {
        assertThat(ExecUtils.isMac("Windows 95")).isFalse();
        assertThat(ExecUtils.isMac("Mac OS")).isTrue();
        assertThat(ExecUtils.isMac("Mac OS X")).isTrue();
    }

    @Test
    public void testIsLinux() throws Exception {
        assertThat(ExecUtils.isLinux( "Microsoft Windows 3.1")).isFalse();
        assertThat(ExecUtils.isLinux("Linux")).isTrue();
        assertThat(ExecUtils.isLinux("AIX")).isTrue();
        assertThat(ExecUtils.isLinux("Digital Unix")).isTrue();
    }

    @Test
    public void testIs64bit() throws Exception {
        assertThat(ExecUtils.is64bits("64")).isTrue();
        assertThat(ExecUtils.is64bits("32")).isFalse();
        assertThat(ExecUtils.is64bits("x86_64")).isTrue();
        assertThat(ExecUtils.is64bits("x86")).isFalse();
    }

    @Test
    public void testIsARM() throws Exception {
        // Test 1) x86
        assertThat(ExecUtils.isARM("x86")).isFalse();
        // Test 2) x86_64
        assertThat(ExecUtils.isARM("x86_64")).isFalse();
        // Test 3) ARM
        assertThat(ExecUtils.isARM("ARM")).isTrue();
    }

    @Test
    public void findExecutable() {
        if (ExecUtils.isWindows()) {
            assertThat(ExecUtils.find("notepad")).isFile();
            assertThat(ExecUtils.findExecutableInSystemPath("notepad")).isFile();
        } else {
            assertThat(ExecUtils.find("ls")).isFile();
            assertThat(ExecUtils.findExecutableInSystemPath("ls")).isFile();
        }
    }
}