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
package org.wisdom.ebean.runtime;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.database.jdbc.service.DataSources;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by clement on 18/02/2014.
 */
@Component(immediate = true)
@Instantiate
public class EbeanExtender implements BundleTrackerCustomizer<EbeanRepository> {


    private final Map<Bundle, EbeanRepository> servers = new HashMap<>();
    private final BundleContext context;

    @Requires
    ApplicationConfiguration configuration;

    @Requires
    DataSources sources;
    private BundleTracker<EbeanRepository> tracker;
    private static final Logger LOGGER = LoggerFactory.getLogger(EbeanExtender.class);
    private BiMap<DataSource, Bundle> usedDataSources = HashBiMap.create();
    //    private final DataSource source;

    public EbeanExtender(BundleContext bc) {
        this.context = bc;
    }

    @Validate
    public void start() {
        tracker = new BundleTracker<>(context, Bundle.ACTIVE, this);
        tracker.open();
    }

    @Invalidate
    public void stop() {
        if (tracker != null) {
            tracker.close();
        }
    }

    @Override
    public EbeanRepository addingBundle(Bundle bundle, BundleEvent event) {
        String list = bundle.getHeaders().get("Ebean-Entities");
        if (list != null && !list.trim().isEmpty()) {
            LOGGER.info("Entities found in " + bundle.getBundleId() + " (" + bundle.getSymbolicName() + ") : " + list);

            String[] classes = list.split(",");

            DataSource source = lookupForDataSource(bundle);
            if (source == null) {
                LOGGER.error("No data source configured or available to persist entities from {} ({})",
                        bundle.getBundleId(), bundle.getSymbolicName());
                return null;
            }
            source = new WrappingDatasource(source);
            try {
                List<Class<?>> clazzes = getClasses(bundle, classes);
                ServerConfig config = new ServerConfig();
                config.setName(bundle.getSymbolicName());
                //TODO This won't work
                config.loadFromProperties();
                config.setDataSource(source);
                if (source == sources.getDataSource()) {
                    config.setDefaultServer(true);
                }
                config.setClasses(clazzes);
                EbeanServer server = EbeanServerFactory.create(config);
                EbeanRepository repository = new EbeanRepository(server, source);
                servers.put(bundle, repository);


                // Instantiate all CrudServices
                for (Class<?> clazz : clazzes) {
                    EbeanCrudService<?> svc = new EbeanCrudService(repository, clazz);
                    repository.addCrud(svc);
                }

                // DDL
                if (!configuration.isProd()) {
                    if (configuration.getBooleanWithDefault("ebean.useDDL", true)) {
                        LOGGER.warn("Generating database schema from DDL");
                        DdlGenerator ddl = new DdlGenerator((SpiEbeanServer) server, config.getDatabasePlatform(), config);
                        String ups = ddl.generateCreateDdl();
                        String down = ddl.generateDropDdl();
                        LOGGER.info("Executing 'down' DDL");
                        executeDDL(server, down);
                        LOGGER.info("Executing 'up' DDL");
                        executeDDL(server, ups);
                    }
                }

                // Register the repository
                repository.register(context);

                return repository;
            } catch (ClassNotFoundException e) {
                usedDataSources.remove(source);
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Helper method that generates the required evolution to properly run Ebean.
     */
    public static String generateEvolutionScript(EbeanServer server, ServerConfig config) {
        DdlGenerator ddl = new DdlGenerator((SpiEbeanServer) server, config.getDatabasePlatform(), config);
        String ups = ddl.generateCreateDdl();
        String downs = ddl.generateDropDdl();

        if (ups == null || ups.trim().isEmpty()) {
            return null;
        }

        return (
                "# --- Created by Ebean DDL\r\n" +
                        "# To stop Ebean DDL generation, remove this comment and start using Evolutions\r\n" +
                        "\r\n" +
                        "# --- !Ups\r\n" +
                        "\r\n" +
                        ups +
                        "\r\n" +
                        "# --- !Downs\r\n" +
                        "\r\n" +
                        downs
        );
    }

    private List<Class<?>> getClasses(Bundle bundle, String[] classes) throws ClassNotFoundException {
        List<Class<?>> list = new ArrayList<>();
        for (String s : classes) {
            try {
                Class c = bundle.loadClass(s);
                list.add(c);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Cannot load entity class " + s + " from bundle " + bundle
                        .getSymbolicName());
            }
        }
        return list;
    }

    /**
     * Retrieve the data source associated to the bundle.
     * The selection is quite simple:
     * If there is a data source with the bundle symbolic name, use it, if not use the 'default' data source.
     * The the found data source is already used, log an error and return null
     *
     * @param bundle the bundle
     * @return the associated data source, {@literal null} if none match or if the matching one is already used.
     */
    private DataSource lookupForDataSource(Bundle bundle) {
        //TODO This is not very dynamic...
        DataSource source = sources.getDataSource(bundle.getSymbolicName());
        if (source == null) {
            source = sources.getDataSource(); // Get the default one.
        }

        if (source == null) {
            LOGGER.error("No data source associated ");
            return null;
        }

        synchronized (this) {
            if (usedDataSources.keySet().contains(source)) {
                LOGGER.error("The bundle {} ({}) contains Ebean entities but the associated data source {} is already " +
                                "used by bundle {} ({})", bundle.getBundleId(), bundle.getSymbolicName(), source,
                        usedDataSources.get(source).getBundleId(), usedDataSources.get(source).getSymbolicName()
                );
            } else {
                usedDataSources.put(source, bundle);
            }
        }
        return source;
    }

    /**
     * A bundle tracked by the {@code BundleTracker} has been modified.
     * <p/>
     * <p/>
     * This method is called when a bundle being tracked by the
     * {@code BundleTracker} has had its state modified.
     *
     * @param bundle The {@code Bundle} whose state has been modified.
     * @param event  The bundle event which caused this customizer method to be
     *               called or {@code null} if there is no bundle event associated
     *               with the call to this method.
     * @param object The tracked object for the specified bundle.
     */
    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, EbeanRepository object) {
        // Do nothing as we track only one state.
    }

    /**
     * A bundle tracked by the {@code BundleTracker} has been removed.
     * <p/>
     * <p/>
     * This method is called after a bundle is no longer being tracked by the
     * {@code BundleTracker}.
     *
     * @param bundle     The {@code Bundle} that has been removed.
     * @param event      The bundle event which caused this customizer method to be
     *                   called or {@code null} if there is no bundle event associated
     *                   with the call to this method.
     * @param repository The tracked object for the specified bundle.
     */
    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, EbeanRepository repository) {
        if (repository != null) {
            // Release data source
            if (usedDataSources.containsValue(bundle)) {
                DataSource source = usedDataSources.inverse().get(bundle);
                usedDataSources.remove(source);
            }
            repository.unregister();
        }
    }

    /**
     * Execute some DDL on a new transaction.
     */
    public void executeDDL(EbeanServer server, String ddl) {

        Transaction t = server.createTransaction();
        try {
            LOGGER.info("Executing " + ddl);
            Connection connection = t.getConnection();
            executeStmt(connection, ddl);
            t.commit();
        } catch (SQLException e) {
            LOGGER.error("Failed to execute DDL", e);
        } finally {
            t.end();
        }

    }

    /**
     * Execute the DDL statement using a PreparedStatement.
     */
    private void executeStmt(Connection c, String ddl) throws SQLException {
        java.sql.PreparedStatement pstmt = null;
        try {
            pstmt = c.prepareStatement(ddl);
            pstmt.execute();

        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.warn("Failed to close PreparedStatement", e);
                }
            }
        }
    }

    /**
     * <code>DataSource</code> wrapper to ensure that every retrieved connection has auto-commit disabled.
     */
    static class WrappingDatasource implements javax.sql.DataSource {

        public java.sql.Connection wrap(java.sql.Connection connection) throws java.sql.SQLException {
            connection.setAutoCommit(false);
            return connection;
        }

        // --

        final javax.sql.DataSource wrapped;

        public WrappingDatasource(javax.sql.DataSource wrapped) {
            this.wrapped = wrapped;
        }

        public java.sql.Connection getConnection() throws java.sql.SQLException {
            return wrap(wrapped.getConnection());
        }

        public java.sql.Connection getConnection(String username, String password) throws java.sql.SQLException {
            return wrap(wrapped.getConnection(username, password));
        }

        public int getLoginTimeout() throws java.sql.SQLException {
            return wrapped.getLoginTimeout();
        }

        public java.io.PrintWriter getLogWriter() throws java.sql.SQLException {
            return wrapped.getLogWriter();
        }

        public void setLoginTimeout(int seconds) throws java.sql.SQLException {
            wrapped.setLoginTimeout(seconds);
        }

        public void setLogWriter(java.io.PrintWriter out) throws java.sql.SQLException {
            wrapped.setLogWriter(out);
        }

        public boolean isWrapperFor(Class<?> iface) throws java.sql.SQLException {
            return wrapped.isWrapperFor(iface);
        }

        public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
            return wrapped.unwrap(iface);
        }

        public java.util.logging.Logger getParentLogger() {
            return null;
        }

    }
}
