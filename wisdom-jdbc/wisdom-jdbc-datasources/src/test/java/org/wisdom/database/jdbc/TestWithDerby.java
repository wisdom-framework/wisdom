package org.wisdom.database.jdbc;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.After;
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
 * Check the data source manager with Derby.
 */
public class TestWithDerby {


    private DataSourceFactory factory;

    @After
    public void cleanup() {
        File log = new File("derby.log");
        if (log.isFile()) {
            log.delete();
        }
    }

    @Test
    public void testDerby() throws ClassNotFoundException, SQLException {
        BundleContext context = prepareContext();

        Map<String, Object> map = ImmutableMap.<String, Object>of(
                "default.driver", "org.apache.derby.jdbc.EmbeddedDriver",
                "default.url", "jdbc:derby:memory:sample;create=true",
                "default.logStatements", "true"
        );
        MapConfiguration derbyConf = new MapConfiguration(map);
        Configuration conf = new ConfigurationImpl(derbyConf);

        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getConfiguration(BoneCPDataSources.DB_CONFIGURATION_PREFIX)).thenReturn(conf);

        BoneCPDataSources sources = new BoneCPDataSources(context, configuration);
        sources.bindFactory(factory, ImmutableMap.of(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
                EmbeddedDriver.class.getName()));


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
        assertThat(results.getString(2).trim()).isEqualTo("Caribou");
        results.close();

        sources.onStop();
    }

    private BundleContext prepareContext() throws ClassNotFoundException, SQLException {
        Bundle bundle = mock(Bundle.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundle()).thenReturn(bundle);
        when(bundle.loadClass(anyString())).thenAnswer(new Answer<Class>() {
            @Override
            public Class answer(InvocationOnMock invocation) throws Throwable {
                return TestWithDerby.class.getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        });

        factory = mock(DataSourceFactory.class);
        when(factory.createDriver(any(Properties.class))).thenReturn(new EmbeddedDriver());
        return context;
    }

}
