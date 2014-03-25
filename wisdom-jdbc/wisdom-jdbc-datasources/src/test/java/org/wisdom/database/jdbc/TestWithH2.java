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
package org.wisdom.database.jdbc;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.configuration.MapConfiguration;
import org.h2.Driver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.configuration.ConfigurationImpl;
import org.wisdom.database.jdbc.impl.BoneCPDataSources;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the data source manager with H2.
 */
public class TestWithH2 {

    public static final File DB_FILE = new File("target/h2test.db.h2.db");

    @Before
    public void setUp() {
        if (DB_FILE.isFile()) {
            DB_FILE.delete();
        }
    }

    @Test
    public void testH2Memory() throws ClassNotFoundException, SQLException {
        Bundle bundle = mock(Bundle.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundle()).thenReturn(bundle);
        when(bundle.loadClass(anyString())).thenAnswer(new Answer<Class>() {
            @Override
            public Class answer(InvocationOnMock invocation) throws Throwable {
                return TestWithH2.class.getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        });

        DataSourceFactory factory = mock(DataSourceFactory.class);
        when(factory.createDriver(any(Properties.class))).thenReturn(new Driver());

        Map<String, Object> map = ImmutableMap.<String, Object>of(
                "default.driver", "org.h2.Driver",
                "default.url", "jdbc:h2:mem:wisdom",
                "default.logStatements", "true"
        );
        MapConfiguration h2Conf = new MapConfiguration(map);
        Configuration conf = new ConfigurationImpl(h2Conf);

        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getConfiguration(BoneCPDataSources.DB_CONFIGURATION_PREFIX)).thenReturn(conf);

        BoneCPDataSources sources = new BoneCPDataSources(context, configuration);
        sources.bindFactory(factory, ImmutableMap.of(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver"));

        assertThat(sources).isNotNull();
        sources.onStart();

        assertThat(sources.getDataSources()).hasSize(1);
        assertThat(sources.getDataSources()).containsKeys("default");
        assertThat(sources.getDataSource()).isNotNull();
        assertThat(sources.getDataSource("default")).isNotNull();

        sources.getConnection().createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection().createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection().createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection().createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection().createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2)).isEqualTo("Caribou");
        results.close();

        sources.onStop();
    }

    @Test
    public void testH2MemoryWitName() throws ClassNotFoundException, SQLException {
        Bundle bundle = mock(Bundle.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundle()).thenReturn(bundle);
        when(bundle.loadClass(anyString())).thenAnswer(new Answer<Class>() {
            @Override
            public Class answer(InvocationOnMock invocation) throws Throwable {
                return TestWithH2.class.getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        });
        DataSourceFactory factory = mock(DataSourceFactory.class);
        when(factory.createDriver(any(Properties.class))).thenReturn(new Driver());

        Map<String, Object> map = ImmutableMap.<String, Object>of(
                "my.driver", "org.h2.Driver",
                "my.url", "jdbc:h2:mem:wisdom-2",
                "my.logStatements", "true"
        );
        MapConfiguration h2Conf = new MapConfiguration(map);
        Configuration conf = new ConfigurationImpl(h2Conf);

        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getConfiguration(BoneCPDataSources.DB_CONFIGURATION_PREFIX)).thenReturn(conf);

        BoneCPDataSources sources = new BoneCPDataSources(context, configuration);
        sources.bindFactory(factory, ImmutableMap.of(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver"));

        assertThat(sources).isNotNull();
        sources.onStart();

        assertThat(sources.getDataSources()).hasSize(1);
        assertThat(sources.getDataSources()).containsKeys("my");
        assertThat(sources.getDataSource("my")).isNotNull();
        assertThat(sources.getDataSource()).isNull();

        sources.getConnection("my").createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection("my").createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection("my").createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection("my").createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection("my").createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2)).isEqualTo("Caribou");
        results.close();

        sources.onStop();
    }

    @Test
    public void testH2File() throws ClassNotFoundException, SQLException {
        Bundle bundle = mock(Bundle.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundle()).thenReturn(bundle);
        when(bundle.loadClass(anyString())).thenAnswer(new Answer<Class>() {
            @Override
            public Class answer(InvocationOnMock invocation) throws Throwable {
                return TestWithH2.class.getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        });
        DataSourceFactory factory = mock(DataSourceFactory.class);
        when(factory.createDriver(any(Properties.class))).thenReturn(new Driver());

        Map<String, Object> map = ImmutableMap.<String, Object>of(
                "default.driver", "org.h2.Driver",
                "default.url", "jdbc:h2:target/h2test.db",
                "default.logStatements", "true"
        );
        MapConfiguration h2Conf = new MapConfiguration(map);
        Configuration conf = new ConfigurationImpl(h2Conf);

        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getConfiguration(BoneCPDataSources.DB_CONFIGURATION_PREFIX)).thenReturn(conf);

        BoneCPDataSources sources = new BoneCPDataSources(context, configuration);
        sources.bindFactory(factory, ImmutableMap.of(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver"));

        assertThat(sources).isNotNull();
        sources.onStart();

        assertThat(sources.getDataSources()).hasSize(1);
        assertThat(sources.getDataSources()).containsKeys("default");
        assertThat(sources.getDataSource()).isNotNull();
        assertThat(sources.getDataSource("default")).isNotNull();

        sources.getConnection().createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection().createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection().createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection().createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection().createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2)).isEqualTo("Caribou");
        results.close();

        assertThat(DB_FILE).isFile();

        sources.onStop();
    }

}
