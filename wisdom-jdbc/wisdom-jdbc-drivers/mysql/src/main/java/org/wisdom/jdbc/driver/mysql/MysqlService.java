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
package org.wisdom.jdbc.driver.mysql;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.service.jdbc.DataSourceFactory;
import org.wisdom.jdbc.driver.helpers.AbstractDataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;

/**
 * An implementation of the data source factory service for creating MySQL data sources that connect to a MySQL
 * database. The properties specified in the create methods determine how the created object is configured.
 */
@Component
@Provides(
        properties = {
                @StaticServiceProperty(name = DataSourceFactory.OSGI_JDBC_DRIVER_NAME, value = "MySQL",
                        type = "java.lang.String"),
                @StaticServiceProperty(name = DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, value = "com.mysql.jdbc.Driver",
                        type = "java.lang.String")
        }
)
@Instantiate
public class MysqlService extends AbstractDataSourceFactory {

    @Override
    public Driver newJdbcDriver() throws SQLException {
        return new com.mysql.jdbc.Driver();
    }

    @Override
    public DataSource newDataSource() throws SQLException {
        return new MysqlDataSource();
    }

    @Override
    public ConnectionPoolDataSource newConnectionPoolDataSource() throws SQLException {
        return new MysqlConnectionPoolDataSource();
    }

    @Override
    public XADataSource newXADataSource() throws SQLException {
        return new MysqlXADataSource();
    }
}
