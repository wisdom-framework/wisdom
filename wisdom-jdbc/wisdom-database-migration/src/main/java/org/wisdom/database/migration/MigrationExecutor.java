package org.wisdom.database.migration;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationInfo;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.database.jdbc.DataSources;
import sun.plugin.dom.exception.InvalidStateException;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A component applying data base migrations.
 */
@Component(immediate = true)
@Instantiate
public class MigrationExecutor {

    public static final Logger LOGGER = LoggerFactory.getLogger(MigrationExecutor.class);

    @Requires
    ApplicationConfiguration configuration;

    /**
     * Stores the FlyWay instances per data source
     */
    Map<String, Flyway> jobs = new HashMap<>();


    @Bind(optional = true, aggregate = true)
    public void bindDS(DataSource source, Map<String, String> properties) {
        String name = properties.get(DataSources.DATASOURCE_NAME_PROPERTY);
        LOGGER.info("Binding data source {}", name);

        // Create the FlyWay instance for the source if needed.
        File directory = getMigrationDirectory(name);
        if (! jobs.containsKey(name)  && directory.isDirectory()) {
            Flyway flyway = new Flyway();
            flyway.setInitOnMigrate(initOnMigrate(name));
            flyway.setDataSource(source);
            flyway.setLocations(directory.getAbsolutePath());
            jobs.put(name, flyway);
        }
        migrate(name);
    }

    @Unbind
    public void unbindDS(DataSource source, Map<String, String> properties) {
        String name = properties.get(DataSources.DATASOURCE_NAME_PROPERTY);
        LOGGER.info("Unbinding data source {}", name);
        jobs.remove(name);
    }

    public void migrate(String name) {
        if (auto(name)  && jobs.containsKey(name)) {
            LOGGER.info("Executing database migration on {}", name);
            jobs.get(name).migrate();
        } else {
            checkState(name);
        }
    }

    /**
     * Gets the directory that should contain the migration file for the data source with the given name.
     * @param name the name
     * @return the directory
     */
    private File getMigrationDirectory(String name) {
        return new File(configuration.getBaseDir(), "conf/migrations/" + name);
    }

    private void checkState(String name) {
        Flyway fw = jobs.get(name);
        if (fw != null) {
            MigrationInfo[] pendings = fw.info().pending();
            if (pendings.length != 0) {
                List<String> list = new ArrayList<>();
                for (MigrationInfo info : pendings) {
                    list.add(info.getDescription());
                }
                throw new InvalidStateException("The database " + name + " is in an inconsistent state, " +
                        pendings.length + " migration(s) not applied: " + list);

            }
        }
    }

    /**
     * Whether or not the FlyWay initialization must be executed on migration
     * @param name the data source name
     * @return {@literal true} if the initialization needs to be executed, {@literal false} otherwise (default)
     */
    private boolean initOnMigrate(String name) {
        return configuration.getBooleanWithDefault("db." + name + ".migration.initOnMigrate", false);
    }

    /**
     * Whether or not the migration should be applied automatically.
     * @param name the data source name
     * @return {@literal true} if the migrations should be applied automatically, {@literal false} otherwise. In dev
     * mode default is true, while in test and prod mode, the default is false.
     */
    private boolean auto(String name) {
        return configuration.getBooleanWithDefault("db." + name + ".migration.auto", configuration.isDev());
    }
}
