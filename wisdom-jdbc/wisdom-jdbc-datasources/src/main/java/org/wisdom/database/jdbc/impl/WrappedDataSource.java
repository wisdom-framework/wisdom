package org.wisdom.database.jdbc.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.database.jdbc.DataSources;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * An implementation of data source delegating call an a wrapped data source. This implementation act as a 'weak
 * reference' as the wrapped data source may not be available.
 */
public class WrappedDataSource implements DataSource {

    private final Configuration configuration;
    private final String name;
    private DataSource wrapped;
    private ServiceRegistration<DataSource> registration;

    public WrappedDataSource(String name, Configuration conf) {
        this.name = name;
        this.configuration = conf;
    }

    public String getName() {
        return name;
    }

    public synchronized void set(DataSource source) {
        this.wrapped = source;
    }

    public synchronized void unset() {
        this.wrapped = null;
    }

    public synchronized boolean isAvailable() {
        return wrapped != null;
    }

    /**
     * <p>Attempts to establish a connection with the data source that
     * this <code>DataSource</code> object represents.
     *
     * @return a connection to the data source
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public Connection getConnection() throws SQLException {
        return wrapped.getConnection();
    }

    /**
     * <p>Attempts to establish a connection with the data source that
     * this <code>DataSource</code> object represents.
     *
     * @param username the database user on whose behalf the connection is
     *                 being made
     * @param password the user's password
     * @return a connection to the data source
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return wrapped.getConnection(username, password);
    }

    /**
     * <p>Retrieves the log writer for this <code>DataSource</code>
     * object.
     * <p/>
     * <p>The log writer is a character output stream to which all logging
     * and tracing messages for this data source will be
     * printed.  This includes messages printed by the methods of this
     * object, messages printed by methods of other objects manufactured
     * by this object, and so on.  Messages printed to a data source
     * specific log writer are not printed to the log writer associated
     * with the <code>java.sql.DriverManager</code> class.  When a
     * <code>DataSource</code> object is
     * created, the log writer is initially null; in other words, the
     * default is for logging to be disabled.
     *
     * @return the log writer for this data source or null if
     * logging is disabled
     * @throws java.sql.SQLException if a database access error occurs
     * @see #setLogWriter
     * @since 1.4
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return wrapped.getLogWriter();
    }

    /**
     * <p>Sets the log writer for this <code>DataSource</code>
     * object to the given <code>java.io.PrintWriter</code> object.
     * <p/>
     * <p>The log writer is a character output stream to which all logging
     * and tracing messages for this data source will be
     * printed.  This includes messages printed by the methods of this
     * object, messages printed by methods of other objects manufactured
     * by this object, and so on.  Messages printed to a data source-
     * specific log writer are not printed to the log writer associated
     * with the <code>java.sql.DriverManager</code> class. When a
     * <code>DataSource</code> object is created the log writer is
     * initially null; in other words, the default is for logging to be
     * disabled.
     *
     * @param out the new log writer; to disable logging, set to null
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getLogWriter
     * @since 1.4
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        wrapped.setLogWriter(out);
    }

    /**
     * <p>Sets the maximum time in seconds that this data source will wait
     * while attempting to connect to a database.  A value of zero
     * specifies that the timeout is the default system timeout
     * if there is one; otherwise, it specifies that there is no timeout.
     * When a <code>DataSource</code> object is created, the login timeout is
     * initially zero.
     *
     * @param seconds the data source login time limit
     * @throws java.sql.SQLException if a database access error occurs.
     * @see #getLoginTimeout
     * @since 1.4
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        wrapped.setLoginTimeout(seconds);
    }

    /**
     * Gets the maximum time in seconds that this data source can wait
     * while attempting to connect to a database.  A value of zero
     * means that the timeout is the default system timeout
     * if there is one; otherwise, it means that there is no timeout.
     * When a <code>DataSource</code> object is created, the login timeout is
     * initially zero.
     *
     * @return the data source login time limit
     * @throws java.sql.SQLException if a database access error occurs.
     * @see #setLoginTimeout
     * @since 1.4
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return wrapped.getLoginTimeout();
    }

    /**
     * Return the parent Logger of all the Loggers used by this data source. This
     * should be the Logger farthest from the root Logger that is
     * still an ancestor of all of the Loggers used by this data source. Configuring
     * this Logger will affect all of the log messages generated by the data source.
     * In the worst case, this may be the root Logger.
     *
     * @return the parent Logger for this data source
     * @throws java.sql.SQLFeatureNotSupportedException if the data source does not use <code>java.util.logging<code>.
     * @since 1.7
     */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return wrapped.getParentLogger();
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * <p/>
     * If the receiver implements the interface then the result is the receiver
     * or a proxy for the receiver. If the receiver is a wrapper
     * and the wrapped object implements the interface then the result is the
     * wrapped object or a proxy for the wrapped object. Otherwise return the
     * the result of calling <code>unwrap</code> recursively on the wrapped object
     * or a proxy for that result. If the receiver is not a
     * wrapper and does not implement the interface, then an <code>SQLException</code> is thrown.
     *
     * @param iface A Class defining an interface that the result must implement.
     * @return an object that implements the interface. May be a proxy for the actual implementing object.
     * @throws java.sql.SQLException If no object found that implements the interface
     * @since 1.6
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return wrapped.unwrap(iface);
    }

    /**
     * Returns true if this either implements the interface argument or is directly or indirectly a wrapper
     * for an object that does. Returns false otherwise. If this implements the interface then return true,
     * else if this is a wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped
     * object. If this does not implement the interface and is not a wrapper, return false.
     * This method should be implemented as a low-cost operation compared to <code>unwrap</code> so that
     * callers can use this method to avoid expensive <code>unwrap</code> calls that may fail. If this method
     * returns true then calling <code>unwrap</code> with the same argument should succeed.
     *
     * @param iface a Class defining an interface.
     * @return true if this implements the interface or directly or indirectly wraps an object that does.
     * @throws java.sql.SQLException if an error occurs while determining whether this is a wrapper
     *                               for an object with the given interface.
     * @since 1.6
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return wrapped.isWrapperFor(iface);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getRequiredDriver() {
        return configuration.get("driver");
    }

    public void register(BundleContext context) {
        if (registration != null) {
            return;
        }
        Dictionary<String, String> props = new Hashtable<>();
        props.put(DataSources.DATASOURCE_NAME_PROPERTY, name);
        registration = context.registerService(DataSource.class, this, props);
    }

    public void unregister() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }

    public DataSource getWrapped() {
        return wrapped;
    }
}
