package org.wisdom.api.crypto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the common hash algorithms.
 */
public class HashTest {

    @Test
    public void testToString() {
        assertThat(Hash.MD5.toString()).isEqualTo("MD5");
        assertThat(Hash.SHA1.toString()).isEqualTo("SHA-1");
        assertThat(Hash.SHA256.toString()).isEqualTo("SHA-256");
        assertThat(Hash.SHA512.toString()).isEqualTo("SHA-512");

    }
}
