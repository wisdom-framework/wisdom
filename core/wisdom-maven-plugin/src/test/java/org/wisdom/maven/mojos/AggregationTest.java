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
package org.wisdom.maven.mojos;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.maven.shared.model.fileset.FileSet;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of aggregation.
 */
public class AggregationTest {

    @Test
    public void testOrderOfFileSet() {
        FileSet fs = new FileSet();
        fs.setDirectory("target/test-classes/aggregation");
        fs.setIncludes(
                ImmutableList.of(
                        "core/the-first-to-aggregate.js",
                        "core/**/*.js",
                        "utils/**/*.js",
                        "plugins/**/*uses*.js",
                        "plugins/**/*.js"
                )
        );
        Aggregation aggregation = new Aggregation();
        aggregation.setFileSets(ImmutableList.of(fs));

        final Collection<File> included = aggregation.getSelectedFiles(new File("target"));
        assertThat(included).hasSize(5);
        // Check order
        assertThat(Iterables.get(included, 0).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/core/the-first-to-aggregate.js").getAbsolutePath());
        assertThat(Iterables.get(included, 1).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/core/a.js").getAbsolutePath());
        assertThat(Iterables.get(included, 4).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/plugins/another-file.js").getAbsolutePath());
    }

    @Test
    public void testOrderOfFileSets() {
        FileSet fs1 = new FileSet();
        fs1.setDirectory("target/test-classes/aggregation");
        fs1.setIncludes(
                ImmutableList.of(
                        "core/the-first-to-aggregate.js",
                        "core/**/*.js",
                        "utils/**/*.js",
                        "plugins/**/*uses*.js",
                        "plugins/**/*.js"
                )
        );

        FileSet fs2 = new FileSet();
        fs2.setDirectory("target/test-classes/aggregation");
        fs2.setIncludes(
                ImmutableList.of(
                        "utils/**/*.js",
                        "plugins/**/*uses*.js",
                        "plugins/**/*.js"
                )
        );

        Aggregation aggregation = new Aggregation();
        aggregation.setFileSets(ImmutableList.of(fs1, fs2));

        final Collection<File> included = aggregation.getSelectedFiles(new File("target"));
        assertThat(included).hasSize(5);
        // Check order
        assertThat(Iterables.get(included, 0).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/core/the-first-to-aggregate.js").getAbsolutePath());
        assertThat(Iterables.get(included, 1).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/core/a.js").getAbsolutePath());
        assertThat(Iterables.get(included, 4).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/plugins/another-file.js").getAbsolutePath());
    }

    @Test
    public void testExcludes() {
        FileSet fs = new FileSet();
        fs.setDirectory("target/test-classes/aggregation");
        fs.setIncludes(
                ImmutableList.of(
                        "core/the-first-to-aggregate.js",
                        "core/**/*.js",
                        "utils/**/*.js",
                        "plugins/**/*uses*.js",
                        "plugins/**/*.js"
                )
        );
        fs.setExcludes(
                ImmutableList.of("**/another-file*")
        );
        Aggregation aggregation = new Aggregation();
        aggregation.setFileSets(ImmutableList.of(fs));

        final Collection<File> included = aggregation.getSelectedFiles(new File("target"));
        assertThat(included).hasSize(4);
        assertThat(Iterables.get(included, 0).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/core/the-first-to-aggregate.js").getAbsolutePath());
        assertThat(Iterables.get(included, 1).getAbsolutePath()).isEqualToIgnoringCase(
                new File("target/test-classes/aggregation/core/a.js").getAbsolutePath());
    }

}