package org.wisdom.jdbc.driver.hsql;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.hsqldb.jdbc.JDBCDataSource;
import org.hsqldb.jdbc.pool.JDBCPooledDataSource;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.osgi.service.jdbc.DataSourceFactory;
import org.wisdom.jdbc.driver.helpers.AbstractDataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;

/**
 * An implementation of the data source factory service for creating HSQLDB data sources that connect to a HSQLDB database
 * either in file or memory. The properties specified in the create methods determine how the created
 * object is configured.
 * <p/>
 * Sample code for obtaining a HSQLDB server data source:
 * <p/>
 * This service supports a URL-based data source. The following 3 properties
 * need to provided.
 * <p/>
 * props.put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:h2-mem");
 * props.put(DataSourceFactory.JDBC_USER, "user");
 * props.put(DataSourceFactory.JDBC_PASSWORD, "password");
 */
@Component
@Provides(
        properties = {
                @StaticServiceProperty(name = DataSourceFactory.OSGI_JDBC_DRIVER_NAME, value = "HSQL",
                        type = "java.lang.String"),
                @StaticServiceProperty(name = DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, value = "org.hsqldb.jdbc.JDBCDriver",
                        type = "java.lang.String")
        }
)
@Instantiate
public class HsqlDbService extends AbstractDataSourceFactory {

    @Override
    public Driver newJdbcDriver() throws SQLException {
        return new org.hsqldb.jdbc.JDBCDriver();
    }

    @Override
    public DataSource newDataSource() throws SQLException {
        return new JDBCDataSource();
    }

    @Override
    public ConnectionPoolDataSource newConnectionPoolDataSource() throws SQLException {
        return new JDBCPooledDataSource();
    }

    @Override
    public XADataSource newXADataSource() throws SQLException {
        return new JDBCXADataSource();
    }
}
