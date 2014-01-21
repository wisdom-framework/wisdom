package org.wisdom.configuration;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Check the configuration management behavior.
 */
public class ApplicationConfigurationTest {

    @After
    public void tearDown() {
        System.clearProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION);
        System.clearProperty("sys");
    }

    @Test
    public void testLoading() {
       System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
       ApplicationConfiguration configuration = new ApplicationConfiguration();
       assertThat(configuration).isNotNull();
       assertThat(configuration.get(ApplicationConfiguration.APPLICATION_SECRET)).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadingNonExistingFile() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/do_not_exist.conf");
        // The next instruction should thrown an IllegalStateException.
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        fail("Should not have been able to create the configuration " + configuration);
    }

    @Test
    public void testBaseDir() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        assertThat(configuration).isNotNull();
        File file = configuration.getBaseDir();
        assertThat(file.isDirectory()).isTrue();
        assertThat(file.getAbsolutePath()).endsWith("test-classes");
    }

    @Test
    public void testGet() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.get("key")).isEqualTo("value");
        assertThat(configuration.get("not_existing")).isNull();
    }

    @Test
    public void testUTF8() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.get("key.utf")).isEqualTo("éøîüå˚πœΩç≈˜µ√ ∑ß˙∫℃");
    }

    @Test
    public void testGetWithDefault() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.getWithDefault("not_existing", "value")).isEqualTo("value");
    }

    @Test
    public void testGetInteger() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        System.setProperty("sys", "5");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.getInteger("key.int")).isEqualTo(1);
        assertThat(configuration.getIntegerWithDefault("key.int", 2)).isEqualTo(1);
        assertThat(configuration.getInteger("sys")).isEqualTo(5);
        assertThat(configuration.getIntegerWithDefault("key.int.no", 2)).isEqualTo(2);
        assertThat(configuration.get("key.int")).isEqualTo("1");
    }
    
    @Test
    public void testGetLong() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.getLong("key.long")).isEqualTo(9999999999999L);
        assertThat(configuration.getLongWithDefault("key.long", 2L)).isEqualTo(9999999999999L);
        assertThat(configuration.getLongWithDefault("key.long.no", 2L)).isEqualTo(2L);
    }

    @Test
    public void testGetBoolean() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        System.setProperty("sys", "true");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.getBoolean("sys")).isTrue();
        assertThat(configuration.getBoolean("key.bool")).isTrue();
        assertThat(configuration.getBooleanWithDefault("key.bool", false)).isTrue();
        assertThat(configuration.getBoolean("key.bool.2")).isFalse();
        assertThat(configuration.getBoolean("key.bool.3")).isFalse();
        assertThat(configuration.getBoolean("key.bool.4")).isTrue();
        assertThat(configuration.getBooleanWithDefault("key.int.no", true)).isTrue();
        assertThat(configuration.getBooleanWithDefault("key.int.no", false)).isFalse();
    }

    @Test
    public void testGetFile() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        File file = configuration.getFileWithDefault("other.conf", (String) null);
        assertThat(file.isFile()).isTrue();
    }

    @Test
    public void testGetOrDie() {
        System.setProperty(ApplicationConfiguration.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfiguration();

        assertThat(configuration.getOrDie("key")).isEqualTo("value");
        try {
            configuration.getOrDie("no");
            fail("The 'no' argument should have thrown an exception");
        } catch (IllegalArgumentException e) {
            // Ok.
        }

        assertThat(configuration.getIntegerOrDie("key.int")).isEqualTo(1);
        try {
            configuration.getIntegerOrDie("no");
            fail("The 'no' argument should have thrown an exception");
        } catch (IllegalArgumentException e) {
            // Ok.
        }

        assertThat(configuration.getBooleanOrDie("key.bool")).isTrue();
        try {
            configuration.getBooleanOrDie("no");
            fail("The 'no' argument should have thrown an exception");
        } catch (IllegalArgumentException e) {
            // Ok.
        }
    }




}
