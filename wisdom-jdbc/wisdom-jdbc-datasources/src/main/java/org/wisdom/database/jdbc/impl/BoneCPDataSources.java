package org.wisdom.database.jdbc.impl;

import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.PoolUtil;
import com.jolbox.bonecp.hooks.AbstractConnectionHook;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.database.jdbc.DataSources;
import org.wisdom.database.jdbc.utils.ClassLoaders;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;


/**
 * The implementation of the data sources service using the Bone CP connection pool
 * (http://http://jolbox.com/index.html).
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

    private Map<String, DataSource> sources = new HashMap<>();

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
     *
     * @return the map of name -> data source, empty if none.
     */
    @Override
    public Map<String, DataSource> getDataSources() {
        return Collections.unmodifiableMap(new HashMap<>(sources));
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
    public void onStart() {
        // Detect all db configurations and create the data sources.
        Set<String> names = new LinkedHashSet<>();
        Set<String> set = dbConfiguration.asMap().keySet();
        for (String s : set) {
            if (s.contains(".")) {
                names.add(s.substring(0, s.indexOf(".")));
            }
        }
        LOGGER.info(names.size() + " data source(s) identified from the configuration : {}", names);

        for (String name : names) {
            Configuration conf = dbConfiguration.getConfiguration(name);
            DataSource source = createDataSource(name, conf);
            if (source == null) {
                LOGGER.error("The data source '{}' cannot be created, check the configuration", name);
            }
            sources.put(name, source);
        }

        // Try to open a connection to each data source.
        // Register the data sources as services.
        for (Map.Entry<String, DataSource> entry : sources.entrySet()) {
            try {
                entry.getValue().getConnection().close();
                LOGGER.info("Connection successful to data source '{}'", entry.getKey());
                Dictionary<String, String> props = new Hashtable<>();
                props.put("datasource.name", entry.getKey());
                context.registerService(DataSource.class, entry.getValue(), props);
            } catch (SQLException e) {
                LOGGER.error("The data source '{}' is configured but the connection failed", entry.getKey(), e);
            }
        }
    }

    @Invalidate
    public void onStop() {
        // Close all data sources
        for (Map.Entry<String, DataSource> entry : sources.entrySet()) {
            shutdownPool(entry.getValue());
            LOGGER.info("Data source '{}' closed", entry.getKey());
        }

        // Unregister all drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                LOGGER.error("An exception occurred while un-registering the JDBC driver '{}'", driver, e);
            }
        }
    }

    private Driver registerDriver(String driverClassName) {
        try {
            Class<Driver> clazz = (Class<Driver>) ClassLoaders.loadClass(context, driverClassName);
            Driver driver = clazz.newInstance();
            DriverManager.registerDriver(driver);
            return driver;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Cannot load the driver class " + driverClassName + ", can't find any bundle exporting the " +
                    "package", e);
        } catch (SQLException e) {
            LOGGER.error("Cannot register the driver '" + driverClassName + "'", e);
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Cannot create the driver instance from class '" + driverClassName + "'", e);
        }
        return null;
    }

    private DataSource createDataSource(String dsName, Configuration dbConf) {
        final BoneCPDataSource datasource = new BoneCPDataSource();

        String driver = dbConf.getWithDefault("driver", null);
        if (driver == null) {
            LOGGER.error("The data source " + dsName + " has not driver classname - " + getPropertyKey(dsName,
                    "driver") + " property not set");
            return null;
        } else {
            Driver instance = registerDriver(driver);
            if (instance == null) {
                return null;
            }
            datasource.setClassLoader(instance.getClass().getClassLoader());
        }

        final boolean autocommit = dbConf.getBooleanWithDefault("autocommit", true);
        final boolean readOnly = dbConf.getBooleanWithDefault("readOnly", false);

        final int isolationLevel = getIsolationLevel(dsName, dbConf);

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
                double timeMs = timeElapsedInNs / 1000;
                String query = PoolUtil.fillLogParams(sql, logParams);
                LOGGER.warn("Query execute time limit exceeded ({}ms) - query: {}", timeMs, query);
            }
        });

        String url = dbConf.getWithDefault("url", null);
        if (url == null) {
            LOGGER.error("The data source " + dsName + " has url - " + getPropertyKey(dsName,
                    "url") + " property not set");
            return null;
        }

        boolean populated = Patterns.populate(datasource, url, isDev);
        if (populated) {
            LOGGER.debug("Data source metadata ('{}') populated from the given url", dsName);
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

        return datasource;
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
        }
        return isolationLevel;
    }

    private void shutdownPool(DataSource source) {
        if (source instanceof BoneCPDataSource) {
            ((BoneCPDataSource) source).close();
        } else {
            throw new IllegalArgumentException("Cannot close a data source not managed by the manager :" + source);
        }
    }

    private String getPropertyKey(String dsName, String propertyName) {
        return DB_CONFIGURATION_PREFIX + "." + dsName + "." + propertyName;
    }

}
