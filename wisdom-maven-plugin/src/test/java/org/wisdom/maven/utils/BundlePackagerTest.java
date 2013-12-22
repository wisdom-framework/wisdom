package org.wisdom.maven.utils;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Check bundle packager.
 */
public class BundlePackagerTest {

    @Test
    public void testExportPackageHeuristics() {
        assertThat(BundlePackager.shouldBeExported("")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.service")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.service.data")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.services")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.services.misc.exception")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.api")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.api.svc")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.apis")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.apis.svc")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.apiculteur.svc")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.service4.svc")).isFalse();
    }


}
