package org.wisdom.database.jdbc.impl;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.PoolUtil;
import com.jolbox.bonecp.hooks.AbstractConnectionHook;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.database.jdbc.DataSources;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


/**
 * The implementation of the data sources service using the Bone CP connection pool
 * (http://http://jolbox.com/index.html).
 * <p/>
 * <p/>
 * TODO We have a leak on the drivers, we should listen for bundles and release the driver instance when the bundle
 * leaves.
 */
@Component
@Provides
@Instantiate
public class BoneCPDataSources implements DataSources {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoneCPDataSources.class);
    public static final String DB_CONFIGURATION_PREFIX = "db";

    private final Configuration dbConfiguration;
    private final BundleContext context;

    /**
     * A boolean indicating if the wisdom server is running in 'dev' mode.
     */
    private final boolean isDev;

    private Map<String, WrappedDataSource> sources = new HashMap<>();

    private Map<String, DataSourceFactory> drivers = new HashMap<>();

    public BoneCPDataSources(BundleContext context, @Requires ApplicationConfiguration configuration) {
        this.context = context;
        this.dbConfiguration = configuration.getConfiguration(DB_CONFIGURATION_PREFIX);
        this.isDev = configuration.isDev();
    }

    @Override
    public Connection getConnection(String database, boolean autocommit) {
        DataSource ds = getDataSource(database);
        if (ds == null) {
            return null;
        }
        try {
            Connection connection = ds.getConnection();
            connection.setAutoCommit(autocommit);
            return connection;
        } catch (SQLException e) {
            LOGGER.error("Cannot open connection on data source '{}", database, e);
            return null;
        }
    }

    /**
     * Gets the data source with the given name.
     *
     * @param database the data source name
     * @return the data source with the given name, {@literal null} if none match
     */
    @Override
    public DataSource getDataSource(String database) {
        return sources.get(database);
    }

    /**
     * Gets the default data source if any.
     *
     * @return the data source with the name 'default', {@literal null} if not defined
     */
    @Override
    public DataSource getDataSource() {
        return sources.get(DEFAULT_DATASOURCE);
    }

    /**
     * Gets the set of data sources (name -> data source)
     * It contains the available data sources only.
     *
     * @return the map of name -> data source, empty if none.
     */
    @Override
    public Map<String, DataSource> getDataSources() {
        HashMap<String, DataSource> map = new HashMap<>();
        for (Map.Entry<String, WrappedDataSource> entry : sources.entrySet()) {
            if (entry.getValue().isAvailable()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    @Override
    public Connection getConnection() {
        return getConnection(DEFAULT_DATASOURCE, true);
    }

    /**
     * Gets a connection on the default data source
     *
     * @param autocommit enables or disables the auto-commit.
     * @return the connection, {@literal null} if the default data source is not configured,
     * or if the connection cannot be opened.
     */
    @Override
    public Connection getConnection(boolean autocommit) {
        return getConnection(DEFAULT_DATASOURCE, autocommit);
    }

    @Override
    public Connection getConnection(String database) {
        return getConnection(database, true);
    }

    @Validate
    public void onStart() throws SQLException {
        // Detect all db configurations and create the data sources.
        Set<String> names = new LinkedHashSet<>();
        if (dbConfiguration == null) {
            LOGGER.info("No data sources configured from the configuration, exiting the data source manager");
            return;
        }
        Set<String> set = dbConfiguration.asMap().keySet();
        for (String s : set) {
            if (s.contains(".")) {
                names.add(s.substring(0, s.indexOf(".")));
            }
        }
        LOGGER.info(names.size() + " data source(s) identified from the configuration : {}", names);

        for (String name : names) {
            Configuration conf = dbConfiguration.getConfiguration(name);
            WrappedDataSource wrapped = new WrappedDataSource(name, conf);
            createDataSource(wrapped);
            if (!wrapped.isAvailable()) {
                LOGGER.error("The data source '{}' cannot be created, the driver is not available or the " +
                        "configuration is invalid", name);
            }
            sources.put(name, wrapped);
        }

        // Try to open a connection to each data source.
        // Register the data sources as services.
        for (Map.Entry<String, WrappedDataSource> entry : sources.entrySet()) {
            try {
                if (entry.getValue().isAvailable()) {
                    entry.getValue().getConnection().close();
                    LOGGER.info("Connection successful to data source '{}'", entry.getKey());
                    entry.getValue().register(context);
                } else {
                    LOGGER.info("The data source '{}' is pending - no driver available", entry.getKey());
                }
            } catch (SQLException e) {
                LOGGER.error("The data source '{}' is configured but the connection failed", entry.getKey(), e);
            }
        }
    }

    @Invalidate
    public void onStop() {
        // Close all data sources
        for (Map.Entry<String, WrappedDataSource> entry : sources.entrySet()) {
            shutdownPool(entry.getValue());
            LOGGER.info("Data source '{}' closed", entry.getKey());
            entry.getValue().unset();
        }

        drivers.clear();
    }

    private void createDataSource(WrappedDataSource source) throws SQLException {
        final BoneCPDataSource datasource = new BoneCPDataSource();
        Configuration dbConf = source.getConfiguration();
        String driver = dbConf.getWithDefault("driver", null);
        if (driver == null) {
            LOGGER.error("The data source " + source.getName() + " has not driver classname - " + getPropertyKey
                    (source.getName(), "driver") + " property not set");
            return;
        } else {
            Driver instance = getDriver(driver);
            if (instance == null) {
                // The driver is not available
                return;
            }
            // OSGi is a bit picky about SQL Driver
            // The fact is that DriverManager is loading drivers from the Classpath Class loader
            // and check that the loaded class is the same as the driver class. Unfortunately, it's not the case
            // so we need a turn around. The idea is to give the driver instance we just create to the pool.
            // We use a special property to achieve this.
            // The pool implementation is enhanced to handle this new property.
            datasource.setClassLoader(instance.getClass().getClassLoader());
            Properties hack = new Properties();
            hack.put(BoneCP.DRIVER_INSTANCE_PROPERTY, instance);
            datasource.setDriverProperties(hack);
        }

        final boolean autocommit = dbConf.getBooleanWithDefault("autocommit", true);
        final boolean readOnly = dbConf.getBooleanWithDefault("readOnly", false);

        final int isolationLevel = getIsolationLevel(source.getName(), dbConf);

        final String catalog = dbConf.getWithDefault("defaultCatalog", null);

        datasource.setConnectionHook(new AbstractConnectionHook() {
            @Override
            public void onCheckIn(ConnectionHandle connection) {
                LOGGER.trace("Check in connection {} [{} leased]", connection.toString(),
                        datasource.getTotalLeased());
            }

            @Override
            public void onCheckOut(ConnectionHandle connection) {
                try {
                    connection.setAutoCommit(autocommit);
                    connection.setTransactionIsolation(isolationLevel);
                    connection.setReadOnly(readOnly);
                    if (catalog != null) {
                        connection.setCatalog(catalog);
                    }
                    LOGGER.trace("Check out connection {} [{} leased]", connection, datasource.getTotalLeased());
                } catch (SQLException e) {
                    LOGGER.error("An exception occurred in the `onCheckOut` of {}", connection, e);
                }
            }

            @Override
            public void onQueryExecuteTimeLimitExceeded(ConnectionHandle handle, Statement statement, String sql, Map<Object, Object> logParams, long timeElapsedInNs) {
                double timeMs = timeElapsedInNs / 1000d;
                String query = PoolUtil.fillLogParams(sql, logParams);
                LOGGER.warn("Query execute time limit exceeded ({}ms) - query: {}", timeMs, query);
            }
        });

        String url = dbConf.getWithDefault("url", null);
        if (url == null) {
            LOGGER.error("The data source " + source.getName() + " has url - " + getPropertyKey(source.getName(),
                    "url") + " property not set");
            return;
        }

        boolean populated = Patterns.populate(datasource, url, isDev);
        if (populated) {
            LOGGER.debug("Data source metadata ('{}') populated from the given url", source.getName());
        }

        datasource.setUsername(dbConf.get("user"));
        datasource.setPassword(dbConf.get("pass"));
        datasource.setPassword(dbConf.get("password"));

        // Pool configuration
        datasource.setPartitionCount(dbConf.getIntegerWithDefault("partitionCount", 1));
        datasource.setMaxConnectionsPerPartition(dbConf.getIntegerWithDefault("maxConnectionsPerPartition", 30));
        datasource.setMinConnectionsPerPartition(dbConf.getIntegerWithDefault("minConnectionsPerPartition", 5));
        datasource.setAcquireIncrement(dbConf.getIntegerWithDefault("acquireIncrement", 1));
        datasource.setAcquireRetryAttempts(dbConf.getIntegerWithDefault("acquireRetryAttempts", 10));
        datasource.setAcquireRetryDelayInMs(dbConf.getIntegerWithDefault("acquireRetryDelay", 1000));
        datasource.setConnectionTimeoutInMs(dbConf.getIntegerWithDefault("connectionTimeout", 1000));
        datasource.setIdleMaxAge(dbConf.getIntegerWithDefault("idleMaxAge", 1000 * 60 * 10),
                java.util.concurrent.TimeUnit.MILLISECONDS);
        datasource.setMaxConnectionAge(dbConf.getIntegerWithDefault("maxConnectionAge", 1000 * 60 * 60),
                java.util.concurrent.TimeUnit.MILLISECONDS);
        datasource.setDisableJMX(dbConf.getBooleanWithDefault("disableJMX", true));
        datasource.setStatisticsEnabled(dbConf.getBooleanWithDefault("statisticsEnabled", false));
        datasource.setIdleConnectionTestPeriod(dbConf.getIntegerWithDefault("idleConnectionTestPeriod", 1000 * 60),
                java.util.concurrent.TimeUnit.MILLISECONDS);
        datasource.setDisableConnectionTracking(dbConf.getBooleanWithDefault("disableConnectionTracking", true));
        datasource.setQueryExecuteTimeLimitInMs(dbConf.getIntegerWithDefault("queryExecuteTimeLimit", 0));

        if (dbConf.get("initSQL") != null) {
            datasource.setInitSQL(dbConf.get("initSQL"));
        }
        datasource.setLogStatementsEnabled(dbConf.getBooleanWithDefault("logStatements", false));
        if (dbConf.get("connectionTestStatement") != null) {
            datasource.setConnectionTestStatement(dbConf.get("connectionTestStatement"));
        }

        //TODO JNDI Binding.

        // Inject the data source.
        source.set(datasource);
    }

    private static int getIsolationLevel(String dsName, Configuration dbConf) {
        String isolation = dbConf.getWithDefault("isolation", "READ_COMMITTED");
        int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
        switch (isolation.toUpperCase()) {
            case "NONE":
                isolationLevel = Connection.TRANSACTION_NONE;
                break;
            case "READ_COMMITTED":
                isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
                break;
            case "READ_UNCOMMITTED":
                isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
                break;
            case "REPEATABLE_READ":
                isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
                break;
            case "SERIALIZABLE":
                isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
                break;
            default:
                LOGGER.error("Unknown isolation level : " + isolation + " for " + dsName);
                break;
        }
        return isolationLevel;
    }

    private void shutdownPool(WrappedDataSource source) {
        if (source.getWrapped() instanceof BoneCPDataSource) {
            ((BoneCPDataSource) source.getWrapped()).close();
        } else {
            throw new IllegalArgumentException("Cannot close a data source not managed by the manager :" + source);
        }
    }

    private String getPropertyKey(String dsName, String propertyName) {
        return DB_CONFIGURATION_PREFIX + "." + dsName + "." + propertyName;
    }

    public synchronized Driver getDriver(String classname) throws SQLException {
        System.out.println(drivers);
        DataSourceFactory factory = drivers.get(classname);
        if (factory != null) {
            return factory.createDriver(null);
        } else {
            return null;
        }
    }

    @Bind(optional = true, aggregate = true)
    public synchronized void bindFactory(DataSourceFactory factory, Map<String, String> properties) {
        String driverClassName = properties.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        drivers.put(driverClassName, factory);
        checkPendingDatasource(driverClassName);
    }

    private void checkPendingDatasource(String driverClassName) {
        for (Map.Entry<String, WrappedDataSource> entry : sources.entrySet()) {
            WrappedDataSource wrapped = entry.getValue();
            if (!wrapped.isAvailable() && driverClassName.equals(wrapped.getRequiredDriver())) {
                // We have a new driver that match one of our unsatisfied data source.
                try {
                    createDataSource(wrapped);
                    if (!wrapped.isAvailable()) {
                        LOGGER.error("The data source '{}' cannot be created, despite the driver just arrives",
                                wrapped.getName());
                    } else {
                        // Register the data source.
                        wrapped.getConnection().close();
                        LOGGER.info("Connection successful to data source '{}'", entry.getKey());
                        wrapped.register(context);
                    }
                } catch (SQLException e) {
                    LOGGER.error("The data source '{}' is configured but the connection failed", entry.getKey(), e);
                }
            }
        }
    }

    @Unbind
    public synchronized void unbindFactory(DataSourceFactory factory, Map<String, String> properties) {
        String driverClassName = properties.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        drivers.remove(driverClassName);
        invalidateDataSources(driverClassName);
    }

    private void invalidateDataSources(String driverClassName) {
        for (Map.Entry<String, WrappedDataSource> entry : sources.entrySet()) {
            WrappedDataSource wrapped = entry.getValue();
            if (wrapped.isAvailable() && driverClassName.equals(wrapped.getRequiredDriver())) {
                // A used driver just left....
                wrapped.unregister();
                wrapped.unset();
            }
        }
    }
}
