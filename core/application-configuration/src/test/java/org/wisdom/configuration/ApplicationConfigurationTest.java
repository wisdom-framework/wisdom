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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.Watcher;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.content.converters.ParamConverterEngine;

/**
 * Check the configuration management behavior.
 */
public class ApplicationConfigurationTest {

    @After
    public void tearDown() {
        System.clearProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION);
        System.clearProperty("sys");
        System.clearProperty("application.mode");
    }

    @Test
    public void testLoading() {
       System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
       ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
       assertThat(configuration).isNotNull();
       assertThat(configuration.get(ApplicationConfiguration.APPLICATION_SECRET)).isNotNull();
        assertThat(configuration.get(ApplicationConfiguration.APPLICATION_BASEDIR)).isNotNull()
                .endsWith("target" + File.separatorChar + "test-classes");
    }

    @Test
    public void testApplicationModes() {
        System.setProperty("application.mode", "DEV");
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.isDev()).isTrue();
        assertThat(configuration.isTest()).isFalse();
        assertThat(configuration.isProd()).isFalse();

        System.setProperty("application.mode", "TEST");
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.isDev()).isFalse();
        assertThat(configuration.isTest()).isTrue();
        assertThat(configuration.isProd()).isFalse();

        System.setProperty("application.mode", "PROD");
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.isDev()).isFalse();
        assertThat(configuration.isTest()).isFalse();
        assertThat(configuration.isProd()).isTrue();

        assertThat(configuration.getBooleanWithDefault("application.watch-configuration", false)).isFalse();

    }

    @Test
    public void testLoadingNonExistingFile() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/do_not_exist.conf");

        // When not existing, we get the system properties.
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration.getConfiguration("os")).isNotNull();
        assertThat(configuration.getConfiguration("os").asMap()).isNotEmpty();
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
        assertThat(configuration.get("key.value")).isEqualTo("value");
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
        assertThat(configuration.getInteger("key.int.1")).isEqualTo(1);
        assertThat(configuration.getIntegerWithDefault("key.int.1", 2)).isEqualTo(1);
        assertThat(configuration.getInteger("sys")).isEqualTo(5);
        assertThat(configuration.getIntegerWithDefault("key.int.no", 2)).isEqualTo(2);
        assertThat(configuration.get("key.int.1")).isEqualTo("1");
    }
    
    @Test
    public void testGetLong() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getLong("key.long")).isEqualTo(9999999999999L);
        assertThat(configuration.getLongWithDefault("key.long", 2L)).isEqualTo(9999999999999L);
        assertThat(configuration.getLongWithDefault("key.long_no", 2L)).isEqualTo(2L);
    }

    @Test
    public void testGetDouble() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getDouble("key.doubles.positive")).isEqualTo(1.1);
        assertThat(configuration.getDouble("key.doubles.negative")).isEqualTo(-1.2);
        assertThat(configuration.getDouble("key.doubles.zero")).isEqualTo(0.0);
        assertThat(configuration.getDoubleWithDefault("key.doubles.positive", 2d)).isEqualTo(1.1);
        assertThat(configuration.getDoubleWithDefault("key.doubles.positive_no", 2d)).isEqualTo(2d);
    }

    @Test
    public void testGetDuration() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key.durations");
        assertThat(conf.getDuration("sec", TimeUnit.SECONDS)).isEqualTo(1);
        assertThat(conf.getDuration("sec", TimeUnit.MILLISECONDS)).isEqualTo(1000);
        assertThat(conf.getDuration("sec", TimeUnit.MILLISECONDS, 2000)).isEqualTo(1000);
        assertThat(conf.getDuration("min", TimeUnit.MINUTES)).isEqualTo(5);

        assertThat(conf.getDuration("sec_non", TimeUnit.MILLISECONDS, 2000)).isEqualTo(2000);
    }

    @Test
    public void testGetBytes() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfiguration configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key.bytes");
        assertThat(conf.getBytes("b")).isEqualTo(1000);
        assertThat(conf.getBytes("i")).isEqualTo(1024);
        assertThat(conf.getBytes("i", 2000)).isEqualTo(1024);
        assertThat(conf.getBytes("no")).isNull();
        assertThat(conf.getBytes("no", 2048)).isEqualTo(2048);
    }

    @Test
    public void testGetBoolean() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        System.setProperty("sys", "true");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getBoolean("sys")).isTrue();
        assertThat(configuration.getBoolean("key.bool.1")).isTrue();
        assertThat(configuration.getBooleanWithDefault("key.bool.1", false)).isTrue();
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

        assertThat(configuration.getOrDie("key.value")).isEqualTo("value");
        try {
            configuration.getOrDie("no");
            fail("The 'no' argument should have thrown an exception");
        } catch (IllegalArgumentException e) {
            // Ok.
        }

        assertThat(configuration.getIntegerOrDie("key.int.1")).isEqualTo(1);
        try {
            configuration.getIntegerOrDie("no");
            fail("The 'no' argument should have thrown an exception");
        } catch (IllegalArgumentException e) {
            // Ok.
        }

        assertThat(configuration.getBooleanOrDie("key.bool.1")).isTrue();
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

        assertThat(conf.getStringArray("array2")).hasSize(3);
        assertThat(conf.getStringArray("array2")).contains("a", "b", "c");

        assertThat(conf.getList("array")).hasSize(3);
        assertThat(conf.getList("array")).contains("a", "b", "c");
        assertThat(conf.getList("array2")).hasSize(3);
        assertThat(conf.getList("array2")).contains("a", "b", "c");

        assertThat(conf.getList("missing")).isEmpty();
        assertThat(conf.getStringArray("missing")).isEmpty();

        assertThat(configuration.getStringArray("key.value")).hasSize(1).contains("value");
        assertThat(configuration.getList("key.value")).hasSize(1).contains("value");
    }

    @Test
    public void testSubConfigurations() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key");
        assertThat(conf.getBoolean("bool.1")).isTrue();
        assertThat(conf.getBooleanWithDefault("bool.1", false)).isTrue();
        assertThat(conf.getBoolean("bool.2")).isFalse();
        assertThat(conf.getBoolean("bool.3")).isFalse();
        assertThat(conf.getBoolean("bool.4")).isTrue();
        assertThat(conf.getBooleanWithDefault("int.no", true)).isTrue();
        assertThat(conf.getBooleanWithDefault("int.no", false)).isFalse();
        assertThat(conf.getInteger("int.1")).isEqualTo(1);
        assertThat(conf.getIntegerWithDefault("int.1", 2)).isEqualTo(1);
        assertThat(conf.getIntegerWithDefault("int.no", 2)).isEqualTo(2);
        assertThat(conf.get("int.1")).isEqualTo("1");

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
        final int numberOfPropertiesStartingWithKey  = 10;
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("key");
        assertThat(conf.asMap()).hasSize(numberOfPropertiesStartingWithKey).containsKeys("bool", "array", "utf",
                "array2", "value", "long", "int", "doubles");

        assertThat((Map<String, Object>) conf.asMap().get("int")).hasSize(2).containsEntry("1", 1).containsEntry
                ("foo", 2);

        assertThat(conf.asProperties()).hasSize(numberOfPropertiesStartingWithKey);
    }

    @Test
    public void testHas() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.has("key")).isTrue();
        assertThat(configuration.has("missing")).isFalse();

        assertThat(configuration.has("key.utf")).isTrue();
        assertThat(configuration.has("key.durations.sec")).isTrue();

        Configuration config = configuration.getConfiguration("key.durations");
        assertThat(config.has("missing")).isFalse();
        assertThat(config.has("sec")).isTrue();
    }

    @Test
    public void testWatcherAndDeployerRegistration() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration reg = mock(ServiceRegistration.class);
        ServiceRegistration regForConf = mock(ServiceRegistration.class);
        when(context.registerService(eq(Deployer.class), any(Deployer.class), any(Dictionary.class))).thenReturn(reg);
        when(context.registerService(eq(Configuration.class), any(Configuration.class), any(Dictionary.class)))
                .thenReturn(regForConf);
        Watcher watcher = mock(Watcher.class);
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, context);
        configuration.watcher = watcher;
        configuration.manageWatcher(context);
        configuration.start();
        verify(watcher, times(1)).add(any(File.class), anyBoolean());
        verify(context, times(1)).registerService(eq(Deployer.class), any(Deployer.class), any(Dictionary.class));
        configuration.stop();
        verify(watcher, times(1)).removeAndStopIfNeeded(any(File.class));
        verify(reg, times(1)).unregister();
        verify(regForConf, times(5)).unregister();
    }


    @Test
    public void testCustomObject() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/custom.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(new ParamConverterEngine
                (Collections.<ParameterConverter>emptyList(), Collections.<ParameterFactory>emptyList()), null);
        assertThat(configuration.get("app.mode", Mode.class)).isEqualTo(Mode.TEST);
        assertThat(configuration.get("app.mode2", Mode.class)).isNull();
        assertThat(configuration.get("app.mode2", Mode.class, Mode.DEV)).isEqualTo(Mode.DEV);
        assertThat(configuration.get("app.mode2", Mode.class, "PROD")).isEqualTo(Mode.PROD);


        assertThat(configuration.get("app.custom", CustomClass.class).name).isEqualTo("wisdom");
        assertThat(configuration.get("app.custom", CustomClass.class).number).isEqualTo("25");
        assertThat(configuration.get("app.custom2", CustomClass.class, "hello;33").name).isEqualTo("hello");
        assertThat(configuration.get("app.custom2", CustomClass.class)).isNull();
        
        Configuration conf = configuration.getConfiguration("app");

        assertThat(conf.get("mode", Mode.class)).isEqualTo(Mode.TEST);
        assertThat(conf.get("mode2", Mode.class)).isNull();
        assertThat(conf.get("mode2", Mode.class, Mode.DEV)).isEqualTo(Mode.DEV);
        assertThat(conf.get("mode2", Mode.class, "PROD")).isEqualTo(Mode.PROD);


        assertThat(conf.get("custom", CustomClass.class).name).isEqualTo("wisdom");
        assertThat(conf.get("custom", CustomClass.class).number).isEqualTo("25");
        assertThat(conf.get("custom2", CustomClass.class, "hello;33").name).isEqualTo("hello");
        assertThat(conf.get("custom2", CustomClass.class)).isNull();
        
    }

    @Test
    public void testSystemProperties() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/interpolation.conf");
        System.setProperty("val.b", "true");
        System.setProperty("val.i", "2");
        System.setProperty("beta", "false");
        System.setProperty("application.name", "Acme App");
        System.setProperty("version", "5");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(new ParamConverterEngine
                (Collections.<ParameterConverter>emptyList(), Collections.<ParameterFactory>emptyList()), null);

        // Try to load system properties
        assertThat(configuration.get("val.b")).isEqualTo("true");
        assertThat(configuration.getBoolean("val.b")).isTrue();
        assertThat(configuration.get("val.i")).isEqualTo("2");
        assertThat(configuration.getInteger("val.i")).isEqualTo(2);

        // Default on missing system properties
        assertThat(configuration.getWithDefault("missing", "val")).isEqualTo("val");
        assertThat(configuration.getBooleanWithDefault("missing", true)).isTrue();
        assertThat(configuration.getIntegerWithDefault("missing", 23)).isEqualTo(23);

        // Overridden values
        assertThat(configuration.get("application.name")).isEqualTo("Acme App");
        assertThat(configuration.getBoolean("beta")).isFalse();
        assertThat(configuration.getInteger("version")).isEqualTo(5);

        System.clearProperty("val.b");
        System.clearProperty("val.i");
        System.clearProperty("beta");
        System.clearProperty("application.name");
        System.clearProperty("version");
    }

    @Test
    public void testIncludes() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/includes/root.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(new ParamConverterEngine
                (Collections.<ParameterConverter>emptyList(), Collections.<ParameterFactory>emptyList()), null);

        assertThat(configuration.get("root")).isEqualTo("root");

        assertThat(configuration.get("application.name")).isEqualTo("Sample");
        assertThat(configuration.get("application.vendor")).isEqualTo("Acme");
        assertThat(configuration.get("application.version")).isEqualTo("1.0");

        Configuration sub = configuration.getConfiguration("sub");
        assertThat(sub.getInteger("test")).isEqualTo(1);
        assertThat(sub.getInteger("foo")).isEqualTo(1);
        assertThat(sub.get("bar")).isEqualTo("baz");
    }

    @Test
    public void testInterpolationWithSystemProperties() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/interpolation.conf");
        System.setProperty("version", "2");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(new ParamConverterEngine
                (Collections.<ParameterConverter>emptyList(), Collections.<ParameterFactory>emptyList()), null);

        assertThat(configuration.get("application.title")).isEqualTo("Killer App 1.6.2 (2)");
        System.clearProperty("version");
    }

    @Test
    public void testInterpolationWithoutSystemProperties() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/interpolation.conf");
        System.clearProperty("version");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(new ParamConverterEngine
                (Collections.<ParameterConverter>emptyList(), Collections.<ParameterFactory>emptyList()), null);
        assertThat(configuration.get("application.title")).isEqualTo("Killer App 1.6.2 (1)");
    }

    @Test
    public void testCors() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION, "target/test-classes/conf/regular.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("cors");
        assertThat(conf.getBoolean("enabled")).isTrue();
        assertThat(conf.getInteger("max-age")).isEqualTo(86400);

        // Using global path:
        assertThat(configuration.getBoolean("cors.enabled")).isTrue();
        assertThat(configuration.getInteger("cors.max-age")).isEqualTo(86400);
    }

    @Test
    public void testIterationOnKeys() {
        System.setProperty(ApplicationConfigurationImpl.APPLICATION_CONFIGURATION,
                "target/test-classes/conf/iteration.conf");
        ApplicationConfigurationImpl configuration = new ApplicationConfigurationImpl(null, null);
        assertThat(configuration).isNotNull();
        Configuration conf = configuration.getConfiguration("orientdb");
        assertThat(conf.asMap().keySet()).contains("news", "extension").hasSize(2);
        for (String key : conf.asMap().keySet()) {
            Configuration sub = conf.getConfiguration(key);
            assertThat(sub.has("user")).isTrue();
            assertThat(sub.has("pass")).isTrue();
            assertThat(sub.has("package")).isTrue();
            assertThat(sub.has("url")).isTrue();
        }
    }
}
