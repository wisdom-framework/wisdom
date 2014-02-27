package org.wisdom.database.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A service letting application to retrieve data sources and open connections on these data sources. It's a
 * high-level API for getting JDBC connections.
 */
public interface DataSources {

    /**
     * The default data source name.
     */
    public static final String DEFAULT_DATASOURCE = "default";

    /**
     * Gets the data source with the given name.
     * @param database the data source name
     * @return the data source with the given name, {@literal null} if none match
     */
    DataSource getDataSource(String database);

    /**
     * Gets the default data source if any.
     * @return the data source with the name 'default', {@literal null} if not defined
     */
    DataSource getDataSource();

    /**
     * Gets the set of data sources (name -> data source)
     * @return the map of name -> data source, empty if none.
     */
    Map<String, DataSource> getDataSources();

    /**
     * Gets a connection on the default data source.
     * The auto-commit is enabled.
     * @return the connection, {@literal null} if the default data source is not configured,
     * or if the connection cannot be opened.
     */
    Connection getConnection();

    /**
     * Gets a connection on the default data source
     * @param autocommit enables or disables the auto-commit.
     * @return the connection, {@literal null} if the default data source is not configured,
     * or if the connection cannot be opened.
     */
    Connection getConnection(boolean autocommit);

    /**
     * Gets the connection on the given database.
     * The auto-commit is enabled.
     * @param database the data source name
     * @return the connection, {@literal null} if the data source with the given name is not configured,
     * or if the connection cannot be opened.
     */
    Connection getConnection(String database);

    /**
     * Gets the connection on the given database.
     * The auto-commit is enabled.
     * @param database the data source name
     * @param autocommit enables or disables the auto-commit.
     * @return the connection, {@literal null} if the data source with the given name is not configured,
     * or if the connection cannot be opened.
     */
    Connection getConnection(String database, boolean autocommit);
}
