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
package org.wisdom.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Dictionary;

import org.junit.After;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.Watcher;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;

/**
 * Check the configuration management behavior.
 */
public class ApplicationConfigurationTest {

    @After
    public void tearDown() {
        System.clearProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION);
        System.clearProperty("sys");
    }

    @Test
    public void testLoading() {
       System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
       ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
       assertThat(configuration).isNotNull();
       assertThat(configuration.get(ApplicationConfigurationImpl.APPLICATION_SECRET)).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadingNonExistingFile() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/do_not_exist.conf");
        // The next instruction should thrown an IllegalStateException.
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        fail("Should not have been able to create the configuration " + configuration);
    }

    @Test
    public void testBaseDir() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        File file = configuration.getBaseDir();
        assertThat(file.isDirectory()).isTrue();
        assertThat(file.getAbsolutePath()).endsWith("test-classes");
    }

    @Test
    public void testGet() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.get("key")).isEqualTo("value");
        assertThat(configuration.get("not_existing")).isNull();
    }

    @Test
    public void testUTF8() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.get("key.utf")).isEqualTo("éøîüå˚πœΩç≈˜µ√ ∑ß˙∫℃");
    }

    @Test
    public void testGetWithDefault() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getWithDefault("not_existing", "value")).isEqualTo("value");
    }

    @Test
    public void testGetInteger() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        System.setProperty("sys", "5");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getInteger("key.int")).isEqualTo(1);
        assertThat(configuration.getIntegerWithDefault("key.int", 2)).isEqualTo(1);
        assertThat(configuration.getInteger("sys")).isEqualTo(5);
        assertThat(configuration.getIntegerWithDefault("key.int.no", 2)).isEqualTo(2);
        assertThat(configuration.get("key.int")).isEqualTo("1");
    }
    
    @Test
    public void testGetLong() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getLong("key.long")).isEqualTo(9999999999999L);
        assertThat(configuration.getLongWithDefault("key.long", 2L)).isEqualTo(9999999999999L);
        assertThat(configuration.getLongWithDefault("key.long.no", 2L)).isEqualTo(2L);
    }

    @Test
    public void testGetBoolean() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        System.setProperty("sys", "true");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
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
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        File file = configuration.getFileWithDefault("other.conf", (String) null);
        assertThat(file.isFile()).isTrue();
    }

    @Test
    public void testGetOrDie() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);

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

    @Test
    public void testArray() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key");
        assertThat(conf.getStringArray("array")).hasSize(3);
        assertThat(conf.getStringArray("array")).contains("a", "b", "c");
    }

    @Test
    public void testSubConfigurations() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key");
        assertThat(conf.getBoolean("bool")).isTrue();
        assertThat(conf.getBooleanWithDefault("bool", false)).isTrue();
        assertThat(conf.getBoolean("bool.2")).isFalse();
        assertThat(conf.getBoolean("bool.3")).isFalse();
        assertThat(conf.getBoolean("bool.4")).isTrue();
        assertThat(conf.getBooleanWithDefault("int.no", true)).isTrue();
        assertThat(conf.getBooleanWithDefault("int.no", false)).isFalse();
        assertThat(conf.getInteger("int")).isEqualTo(1);
        assertThat(conf.getIntegerWithDefault("int", 2)).isEqualTo(1);
        assertThat(conf.getIntegerWithDefault("int.no", 2)).isEqualTo(2);
        assertThat(conf.get("int")).isEqualTo("1");

        // Not included
        assertThat(conf.get("key")).isNull();
    }

    @Test
    public void testSubSubConfigurations() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key");
        Configuration sub = conf.getConfiguration("bool");
        assertThat(sub.getBoolean("2")).isFalse();
        assertThat(sub.getBoolean("3")).isFalse();
        assertThat(sub.getBoolean("4")).isTrue();
        sub = conf.getConfiguration("int");
        assertThat(sub.getInteger("foo")).isEqualTo(2);
        assertThat(sub.getIntegerWithDefault("no", 2)).isEqualTo(2);
    }

    @Test
    public void testEmptySubConfigurations() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("nope");
        assertThat(conf).isNull();
    }

    @Test
    public void testAllAndProperties() {
        final int numberOfPropertiesStartingWithKey  = 9;
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key");
        assertThat(conf.asMap()).hasSize(numberOfPropertiesStartingWithKey);
        assertThat(conf.asProperties()).hasSize(numberOfPropertiesStartingWithKey);
    }

    @Test
    public void testWatcherAndDeployerRegistration() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration reg = mock(ServiceRegistration.class);
        when(context.registerService(any(Class.class), any(Deployer.class), any(Dictionary.class))).thenReturn(reg);
        Watcher watcher = mock(Watcher.class);
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(context, watcher);
        configuration.start();
        verify(watcher, times(1)).add(any(File.class), anyBoolean());
        verify(context, times(1)).registerService(any(Class.class), any(Deployer.class), any(Dictionary.class));
        configuration.stop();
        verify(watcher, times(1)).removeAndStopIfNeeded(any(File.class));
        verify(reg, times(1)).unregister();
    }
}
