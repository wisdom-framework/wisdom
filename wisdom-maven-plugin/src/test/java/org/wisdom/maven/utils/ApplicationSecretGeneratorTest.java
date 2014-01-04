package org.wisdom.maven.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check secret generation.
 */
public class ApplicationSecretGeneratorTest {

    @Test
    public void generate() {
        String s = ApplicationSecretGenerator.generate();
        System.out.println(s);
        assertThat(s).isNotNull();
        assertThat(s.length()).isEqualTo(64);
    }

}
