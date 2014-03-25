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
package org.wisdom.jdbc.driver.helpers;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.junit.After;
import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.io.File;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the Abstract Data Source Factory.
 */
public class AbstractDataSourceFactoryTest {

    @After
    public void cleanup() {
        File log = new File("derby.log");
        if (log.isFile()) {
            log.delete();
        }
    }

    @Test
    public void testDriverCreation() throws SQLException {
        MyDataSourceFactory factory = new MyDataSourceFactory();
        Properties props = new Properties();
        Driver driver = factory.createDriver(props);
        assertThat(driver).isNotNull();

        driver = factory.createDriver(null);
        assertThat(driver).isNotNull();
    }

    @Test
    public void testDataSourceCreation() throws SQLException {
        MyDataSourceFactory factory = new MyDataSourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, "database");
        props.put(DataSourceFactory.JDBC_USER, "john");
        props.put(DataSourceFactory.JDBC_PASSWORD, "secret");
        DataSource source = factory.createDataSource(props);
        assertThat(source).isNotNull();
    }

    @Test
    public void testXADataSourceCreation() throws SQLException {
        MyDataSourceFactory factory = new MyDataSourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, "database");
        props.put(DataSourceFactory.JDBC_USER, "john");
        props.put(DataSourceFactory.JDBC_PASSWORD, "secret");
        XADataSource source = factory.createXADataSource(props);
        assertThat(source).isNotNull();
    }

    @Test
    public void testConnectionPoolDataSourceCreation() throws SQLException {
        MyDataSourceFactory factory = new MyDataSourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, "database");
        props.put(DataSourceFactory.JDBC_USER, "john");
        props.put(DataSourceFactory.JDBC_PASSWORD, "secret");
        ConnectionPoolDataSource source = factory.createConnectionPoolDataSource(props);
        assertThat(source).isNotNull();
    }

    private class MyDataSourceFactory extends AbstractDataSourceFactory {

        @Override
        public Driver newJdbcDriver() throws SQLException {
            return new EmbeddedDriver();
        }

        @Override
        public DataSource newDataSource() throws SQLException {
            return new EmbeddedDataSource();
        }

        @Override
        public ConnectionPoolDataSource newConnectionPoolDataSource() throws SQLException {
            return new EmbeddedConnectionPoolDataSource();
        }

        @Override
        public XADataSource newXADataSource() throws SQLException {
            return new EmbeddedXADataSource();
        }
    }


}
