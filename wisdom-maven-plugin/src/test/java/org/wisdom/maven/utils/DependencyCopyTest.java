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
