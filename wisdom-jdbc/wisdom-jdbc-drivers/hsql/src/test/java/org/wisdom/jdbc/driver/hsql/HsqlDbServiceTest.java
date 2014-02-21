package org.wisdom.jdbc.driver.hsql;

import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the HSQLDB service.
 */
public class HsqlDbServiceTest {

    private DataSourceFactory svc = new HsqlDbService();

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
