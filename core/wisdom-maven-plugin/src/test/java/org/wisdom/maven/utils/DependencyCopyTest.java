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

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;

/**
 * check the dependency copy
 */
public class DependencyCopyTest {

    @Test
    public void testThatANonExistingFileCannotBeAWebJar() {
        File file = new File("does_not_exist");
        assertThat(DependencyCopy.isWebJar(file)).isFalse();
    }

    @Test
    public void testThatAWebJarIsAWebJar() {
        File file = new File("target/test-classes/webjars/bootstrap-3.1.1.jar");
        assertThat(file).exists();
        assertThat(DependencyCopy.isWebJar(file)).isTrue();
    }

    @Test
    public void testThatABundleIsNotAWebJar() {
        File file = new File("target/test-classes/webjars/org.apache.felix.log-1.0.1.jar");
        assertThat(file).exists();
        assertThat(DependencyCopy.isWebJar(file)).isFalse();
    }

    @Test
    public void testThatATextFileIsNotAWebJar() {
        File file = new File("target/test-classes/webjars/not-a-webjar.txt");
        assertThat(file).exists();
        assertThat(DependencyCopy.isWebJar(file)).isFalse();
    }

    @Test
    public void testThatABundleIsABundle() {
        File file = new File("target/test-classes/webjars/org.apache.felix.log-1.0.1.jar");
        assertThat(file).exists();
        assertThat(isBundle(file)).isTrue();
    }


}
