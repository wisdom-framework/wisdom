package org.wisdom.jdbc.driver.helpers;

import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import static org.wisdom.jdbc.driver.helpers.BeanUtils.setProperty;

/**
 * (Code imported from the Eclipse Gemini project)
 * Abstract factory for creating JDBC data sources and drivers. The properties
 * specified in the create methods determine how the created object is configured.
 * Sample code for obtaining a data source (this example use Derby):
 * <code>
 * DataSourceFactory dsf = service here;
 * Properties props = new Properties();
 * props.put(DataSourceFactory.JDBC_SERVER_NAME, "localhost");
 * props.put(DataSourceFactory.JDBC_PORT_NUMBER, "1527");
 * props.put(DataSourceFactory.JDBC_DATABASE_NAME, "database");
 * props.put(DataSourceFactory.JDBC_USER, "john");
 * props.put(DataSourceFactory.JDBC_PASSWORD, "secret");
 * DataSource ds = dsf.createDataSource(props);
 * </code>
 * Properties are set using reflection on <em>setter</em> methods. Instantiation failed if a property without an
 * associated setter is passed to the method.
 */
public abstract class AbstractDataSourceFactory implements DataSourceFactory {

    /**
     * Creates a DataSource object.
     *
     * @param props The properties that define the DataSource implementation to create and how the DataSource is
     *              configured
     * @return The configured DataSource
     * @throws SQLException If the data source cannot be created
     */
    public DataSource createDataSource(Properties props) throws SQLException {
        if (props == null) {
            props = new Properties();
        }

        DataSource dataSource = newDataSource();
        setBeanProperties(dataSource, props);
        return dataSource;
    }

    /**
     * Create a ConnectionPoolDataSource object.
     *
     * @param props The properties that define the ConnectionPoolDataSource implementation to create and how the
     *              ConnectionPoolDataSource is configured
     * @return The configured ConnectionPoolDataSource
     * @throws SQLException If the data source cannot be created
     */
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
        if (props == null) props = new Properties();
        ConnectionPoolDataSource dataSource = newConnectionPoolDataSource();
        setBeanProperties(dataSource, props);
        return dataSource;
    }

    /**
     * Creates an XADataSource object.
     *
     * @param props The properties that define the XADataSource implementation to create and how the XADataSource is
     *              configured
     * @return The configured XADataSource
     * @throws SQLException If the data source cannot be created
     */
    public XADataSource createXADataSource(Properties props) throws SQLException {
        if (props == null) props = new Properties();
        XADataSource dataSource = newXADataSource();
        setBeanProperties(dataSource, props);
        return dataSource;
    }

    /**
     * Creates a new JDBC Driver instance.
     *
     * @param props The properties used to configure the Driver.  {@literal null} indicates no properties. If the
     *              property cannot be set on the Driver being created then an SQLException must be thrown.
     * @return A configured java.sql.Driver.
     * @throws SQLException If the driver instance cannot be created
     */
    public Driver createDriver(Properties props) throws SQLException {
        Driver driver = newJdbcDriver();
        setBeanProperties(driver, props);
        return driver;
    }

    /**
     * Sets the given properties on the target object
     *
     * @param object the object on which the properties need to be set
     * @param props  the properties
     * @throws SQLException if a property cannot be set.
     */
    static void setBeanProperties(Object object, Properties props)
            throws SQLException {

        if (props != null) {
            Enumeration<?> enumeration = props.keys();
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                setProperty(object, name, props.getProperty(name));
            }
        }
    }

    /**
     * Creates a new instance of the JDBC driver.
     *
     * @return the driver
     * @throws SQLException if the instance cannot be created
     */
    public abstract Driver newJdbcDriver() throws SQLException;

    /**
     * Creates a new instance of the data source.
     *
     * @return the data source
     * @throws SQLException if the instance cannot be created
     */
    public abstract DataSource newDataSource() throws SQLException;

    /**
     * Creates a new instance of the connection pool.
     *
     * @return the connection pool
     * @throws SQLException if the instance cannot be created.
     */
    public abstract ConnectionPoolDataSource newConnectionPoolDataSource() throws SQLException;

    /**
     * Creates a new XA data source.
     *
     * @return the data source
     * @throws SQLException if the instance cannot be created.
     */
    public abstract XADataSource newXADataSource() throws SQLException;
}
