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
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class InstructionsTest {

    @Test
    public void testMerge() throws Exception {
        Properties props1 = new Properties();
        props1.put("Export-Package", "org.foo");
        props1.put("Import-Package", "!*");

        File file = new File("src/test/resources/instructions/instructions.bnd");
        assertThat(Instructions.merge(props1, file))
                .hasSize(3)
                .containsEntry("Export-Package", "org.foo")
                .containsEntry("Import-Package", "*")
                .containsEntry("Private-Package", "org.acme");
    }

    @Test
    public void testMergeAndOverrideExisting() throws Exception {
        Properties props1 = new Properties();
        props1.put("A", "a");
        props1.put("B", "b");

        Properties props2 = new Properties();
        props2.put("A", "aa");
        props2.put("C", "c");

        assertThat(Instructions.mergeAndOverrideExisting(props1, props2))
                .hasSize(3)
                .containsKeys("A", "B", "C")
                .containsEntry("A", "aa");

        assertThat(Instructions.mergeAndOverrideExisting(props1, new Properties()))
                .hasSize(2)
                .containsKeys("A", "B")
                .containsEntry("A", "a");

        assertThat(Instructions.mergeAndOverrideExisting(new Properties(), props2))
                .hasSize(2)
                .containsKeys("A", "C")
                .containsEntry("A", "aa");
    }

    @Test
    public void testMergeAndSkipExisting() throws Exception {
        Properties props1 = new Properties();
        props1.put("A", "a");
        props1.put("B", "b");

        Properties props2 = new Properties();
        props2.put("A", "aa");
        props2.put("C", "c");

        assertThat(Instructions.mergeAndSkipExisting(props1, props2))
                .hasSize(3)
                .containsKeys("A", "B", "C")
                .containsEntry("A", "a");

        assertThat(Instructions.mergeAndSkipExisting(props1, new Properties()))
                .hasSize(2)
                .containsKeys("A", "B")
                .containsEntry("A", "a");

        assertThat(Instructions.mergeAndSkipExisting(new Properties(), props2))
                .hasSize(2)
                .containsKeys("A", "C")
                .containsEntry("A", "aa");
    }

    @Test
    public void testLoadEmptyFile() throws Exception {
        File file = new File("src/test/resources/instructions/empty.bnd");
        assertThat(Instructions.load(file)).isEmpty();
    }

    @Test
    public void testLoadMissingFile() throws Exception {
        File file = new File("src/test/resources/instructions/missing.bnd");
        assertThat(Instructions.load(file)).isEmpty();
    }

    @Test
    public void testLoad() throws IOException {
        File file = new File("src/test/resources/instructions/instructions.bnd");
        assertThat(Instructions.load(file))
                .hasSize(2)
                .containsEntry("Import-Package", "*")
                .containsEntry("Private-Package", "org.acme");
    }

    @Test
    public void testSanitize() throws Exception {
        Properties props = new Properties();

        props.put(new int[4], new HashMap(2));
        props.put("A", new File("B"));
        props.put("4", new HashMap(2));
        props.put("1, two, 3.0", new char[5]);
        props.put("list", ImmutableList.of(1, 2));

        Properties sanitized = Instructions.sanitize(props);

        assertThat(sanitized)
                .containsEntry("A", "B")
                .containsEntry("4", "{}")
                .containsEntry("1, two, 3.0", "\u0000, \u0000, \u0000, \u0000, \u0000")
                .containsEntry("0, 0, 0, 0", "{}")
                .containsEntry("list", "1, 2");
    }
}