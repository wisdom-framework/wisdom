package org.wisdom.jdbc.driver.h2;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.h2.jdbcx.JdbcDataSource;
import org.osgi.service.jdbc.DataSourceFactory;
import org.wisdom.jdbc.driver.helpers.AbstractDataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;

/**
 * An implementation of the data source factory service for creating H2 data sources that connect to a H2 database
 * either in file, memory or server mode. The properties specified in the create methods determine how the created
 * object is configured.
 * <p/>
 * Sample code for obtaining a H2 server data source:
 * <p/>
 * This service supports a URL-based data source. The following 3 properties
 * need to provided.
 * <p/>
 * props.put(DataSourceFactory.JDBC_URL, "jdbc:h2:tcp://dbserv:8084/sample");
 * props.put(DataSourceFactory.JDBC_USER, "user");
 * props.put(DataSourceFactory.JDBC_PASSWORD, "password");
 */
@Component
@Provides(
        properties = {
                @StaticServiceProperty(name = DataSourceFactory.OSGI_JDBC_DRIVER_NAME, value = "H2",
                        type = "java.lang.String"),
                @StaticServiceProperty(name = DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, value = "org.h2.Driver",
                        type = "java.lang.String")
        }
)
@Instantiate
public class H2Service extends AbstractDataSourceFactory {

    @Override
    public Driver newJdbcDriver() throws SQLException {
        return new org.h2.Driver();
    }

    @Override
    public DataSource newDataSource() throws SQLException {
        return new JdbcDataSource();
    }

    @Override
    public ConnectionPoolDataSource newConnectionPoolDataSource() throws SQLException {
        return new JdbcDataSource();
    }

    @Override
    public XADataSource newXADataSource() throws SQLException {
        return new JdbcDataSource();
    }
}
