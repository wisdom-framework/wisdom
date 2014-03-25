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

import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the MySQL service.
 */
public class MySQLServiceTest {

    private DataSourceFactory svc = new MysqlService();

    @Test
    public void testNewJdbcDriver() throws Exception {
        Driver driver = svc.createDriver(null);
        assertThat(driver).isNotNull();
    }

    @Test
    public void testNewDataSource() throws Exception {
        DataSource source = svc.createDataSource(null);
        assertThat(source).isNotNull();
    }

    @Test
    public void testNewConnectionPoolDataSource() throws Exception {
        ConnectionPoolDataSource source = svc.createConnectionPoolDataSource(null);
        assertThat(source).isNotNull();
    }

    @Test
    public void testNewXADataSource() throws Exception {
        XADataSource source = svc.createXADataSource(null);
        assertThat(source).isNotNull();
    }
}
