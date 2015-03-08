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

import com.google.common.collect.Iterators;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class Properties2HoconConverterTest {

    private File root = new File("target/test-classes/properties");

    @Test(expected = IllegalArgumentException.class)
    public void testMissingFile() throws IOException {
        File props = new File(root, "missing.properties");
        Properties2HoconConverter.convert(props, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyFile() throws IOException {
        File props = new File(root, "empty.properties");
        Properties2HoconConverter.convert(props, true);
    }

    @Test
    public void testOnWikipediaSample() throws IOException {
        File props = new File(root, "/wiki.properties");
        File hocon = Properties2HoconConverter.convert(props, true);

        PropertiesConfiguration properties = loadPropertiesWithApacheConfiguration(props);
        assertThat(properties).isNotNull();
        Config config = load(hocon);
        assertThat(properties.isEmpty()).isFalse();
        assertThat(config.isEmpty()).isFalse();

        Iterator<String> iterator = properties.getKeys();
        String[] names = Iterators.toArray(iterator, String.class);
        for (String name : names) {
            if (!name.isEmpty()) {
                // 'cheeses' is not supported by commons-config.
                String o = properties.getString(name);
                String v = config.getString(name);
                assertThat(o).isEqualTo(v);
            }
        }

        assertThat(config.getString("cheeses")).isEmpty();

    }

    @Test
    public void testOnWikipediaSampleUsingRawProperties() throws IOException {
        File props = new File(root, "/wiki.properties");
        File hocon = Properties2HoconConverter.convert(props, true);

        Properties properties = loadProperties(props);

        Config config = load(hocon);
        assertThat(properties.isEmpty()).isFalse();
        assertThat(config.isEmpty()).isFalse();

        for (String name : properties.stringPropertyNames()) {
            // Ignored properties are they are not supported in the 'regular' raw format.
            if (name.equalsIgnoreCase("targetCities")  || name.equalsIgnoreCase("Los")) {
                continue;
            }
            String o = (String) properties.get(name);
            String v = config.getString(name);
            assertThat(o).isEqualTo(v);
        }

    }

    @Test
    public void testTypes() throws IOException {
        File props = new File(root, "/types.properties");
        File hocon = Properties2HoconConverter.convert(props, true);

        PropertiesConfiguration properties = loadPropertiesWithApacheConfiguration(props);

        Config config = load(hocon);
        assertThat(properties).isNotNull();
        assertThat(properties.isEmpty()).isFalse();
        assertThat(config.isEmpty()).isFalse();

        assertThat(config.getString("conf.string")).isEqualTo("foo");
        assertThat(config.getString("conf.int")).isEqualTo("1");
        assertThat(config.getInt("conf.int")).isEqualTo(1);
        assertThat(config.getNumber("conf.int")).isEqualTo(1);

        assertThat(config.getString("conf.float")).isEqualTo("1.1");
        assertThat(config.getNumber("conf.float")).isEqualTo(1.1);
        assertThat(config.getNumber("conf.float")).isEqualTo(1.1d);
        assertThat(config.getDouble("conf.float")).isEqualTo(1.1d);

        assertThat(config.getBoolean("conf.boolean.true")).isTrue();
        assertThat(config.getBoolean("conf.boolean.false")).isFalse();
        assertThat(config.getBoolean("conf.boolean.yes")).isTrue();
        assertThat(config.getBoolean("conf.boolean.no")).isFalse();
        assertThat(config.getBoolean("conf.boolean.on")).isTrue();
        assertThat(config.getBoolean("conf.boolean.off")).isFalse();

        assertThat(config.getDuration("conf.unit.time", TimeUnit.MILLISECONDS)).isEqualTo(10l);
        assertThat(config.getDuration("conf.unit.time", TimeUnit.MICROSECONDS)).isEqualTo(10000l);

        assertThat(config.getBytes("conf.unit.size")).isEqualTo(512*1024);
        assertThat(config.getBytes("conf.unit.size2")).isEqualTo(10);

        assertThat(config.getIntList("conf.list.int")).containsExactly(1, 2, 3);
        assertThat(config.getStringList("conf.list.string")).containsExactly("a", "b", "c");

        assertThat(config.hasPath("conf.missing")).isFalse();

        assertThat(config.getString("conf.quotes")).isEqualTo("http://example.com");
    }

    @Test
    public void testMonitor() throws IOException {
        File props = new File(root, "/monitor.properties");
        File hocon = Properties2HoconConverter.convert(props, true);

        Config config = load(hocon);
        assertThat(config.isEmpty()).isFalse();

        assertThat(config.getString("application.secret")).isEqualTo("I;" +
                ">qOs/VgFe?l@>Kn/RGa0p9b1ji?Kg7uhjAPHdIO8>@<em_AFs[BAMUQ0D]eOLV");

        assertThat(config.getBoolean("monitor.http.enabled")).isTrue();
        assertThat(config.getBoolean("monitor.jmx.enabled")).isTrue();
        assertThat(config.getBoolean("monitor.auth.enabled")).isTrue();
        assertThat(config.getString("monitor.auth.username")).isEqualTo("admin");
        assertThat(config.getString("monitor.auth.password")).isEqualTo("admin");
    }

    @Test
    public void testOpenJPASample() throws IOException {
        File props = new File(root, "/openjpa-sample.properties");
        File hocon = Properties2HoconConverter.convert(props, true);

        Config config = load(hocon);
        assertThat(config.isEmpty()).isFalse();

        assertThat(config.getString("application.secret")).isEqualTo
                ("8pHS=Y/GI>fmUU=LVPKfrgilk^hBk?aOB0a4CFLCg@JX=JHwTvsc7g;QQWl?;IDO");

        assertThat(config.getBoolean("monitor.http.enabled")).isTrue();
        assertThat(config.getBoolean("monitor.jmx.enabled")).isTrue();
        assertThat(config.getBoolean("monitor.auth.enabled")).isTrue();
        assertThat(config.getString("monitor.auth.username")).isEqualTo("admin");
        assertThat(config.getString("monitor.auth.password")).isEqualTo("admin");

        assertThat(config.getBoolean("documentation.standalone")).isFalse();

        assertThat(config.getString("db.todo.driver")).isEqualTo("org.h2.Driver");
        assertThat(config.getString("db.todo.url")).isEqualTo("jdbc:h2:database/todo.db");
    }

    @Test
    public void testCornerCases() throws IOException {
        File props = new File(root, "/corner-cases.properties");
        File hocon = Properties2HoconConverter.convert(props, true);

        Config config = load(hocon);
        assertThat(config.isEmpty()).isFalse();

        // 1 - weird keys
        // Must use quotes
        assertThat(config.getString("\":=\"")).isEqualTo("weird");
        assertThat(config.getString("with spaces")).isEqualTo("spaces");
        assertThat(config.getString("3")).isEqualTo("number");

        // 2 - Multi lines
        assertThat(config.getString("multilines")).contains("foo,").contains("bar,").contains("baz");
        assertThat(config.getString("multilines_with_empty_blank_line")).isEqualTo("foo, bar, baz");

        // 3 - The different syntax
        assertThat(config.getString("truth.1")).isEqualTo("Beauty");
        assertThat(config.getString("truth.2")).isEqualTo("Beauty");
        assertThat(config.getString("truth.3")).isEqualTo("Beauty");

        // 4 - The key only
        assertThat(config.getString("cheeses")).isEqualTo("");

        // 5 - Interpolation
        assertThat(config.getString("foo.interpolated")).isEqualTo("baz - hello");
    }



    private Config load(File hocon) {
        return ConfigFactory.parseFile(hocon).resolve();
    }

    public final PropertiesConfiguration loadPropertiesWithApacheConfiguration(File file) {

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setEncoding("utf-8");
        propertiesConfiguration.setDelimiterParsingDisabled(true);
        propertiesConfiguration.setFile(file);
        propertiesConfiguration.setListDelimiter(',');
        propertiesConfiguration.getLayout().setSingleLine("application.secret", true);

        try {
            propertiesConfiguration.load(file);
        } catch (ConfigurationException e) {
            return null;
        }

        return propertiesConfiguration;
    }

    public final Properties loadProperties(File props) throws IOException {
        Properties properties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(props);
            properties.load(fis);
            return properties;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

}