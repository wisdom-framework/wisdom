package org.wisdom.maven.node;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Check the NPM behavior.
 */
public class NPMTest {

    @Test
    public void testExtractVersionFromPackageJson() {
        Log log = mock(Log.class);
        File coffeescript = new File("target/test-classes/package-json/coffeescript");
        String version = NPM.getVersionFromNPM(coffeescript, log);
        assertThat(version).isEqualTo("1.6.3");

        File less = new File("target/test-classes/package-json/less");
        version = NPM.getVersionFromNPM(less, log);
        assertThat(version).isEqualTo("1.5.0");

        File doesNotExist = new File("target/test-classes/package-json/nope");
        version = NPM.getVersionFromNPM(doesNotExist, log);
        assertThat(version).isEqualTo("0.0.0");
    }
}
