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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks packages.
 */
public class PackagesTest {
    @Test
    public void testJunit() throws Exception {
        StringBuilder builder = new StringBuilder();
        Packages.junit(builder);
        assertThat(builder.toString()).contains("org.junit");
        assertThat(builder.toString()).contains("junit.framework");
    }

    @Test
    public void testWisdomtest() throws Exception {
        StringBuilder builder = new StringBuilder();
        Packages.wisdomtest(builder);
        assertThat(builder.toString()).contains("org.wisdom.test")
            .contains("org.wisdom.test.shared");
    }

    @Test
    public void testJavaxinject() throws Exception {
        StringBuilder builder = new StringBuilder();
        Packages.javaxinject(builder);
        assertThat(builder.toString()).contains("javax.inject");
    }

    @Test
    public void testAssertj() throws Exception {
        StringBuilder builder = new StringBuilder();
        Packages.assertj(builder);
        assertThat(builder.toString()).contains("org.assertj.core.api");
    }
}
