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

    @Test
    public void testOsgihelpers() throws Exception {
        StringBuilder builder = new StringBuilder();
        Packages.osgihelpers(builder);
        assertThat(builder.toString()).contains("org.ow2.chameleon.testing.helpers");
    }
}
