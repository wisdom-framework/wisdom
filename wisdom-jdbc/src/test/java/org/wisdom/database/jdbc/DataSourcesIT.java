package org.wisdom.database.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the data source manager inside Wisdom.
 */
public class DataSourcesIT extends WisdomTest {

    @Inject
    DataSources sources;

    @Test
    public void testDerby() throws SQLException, InvalidSyntaxException {
         assertThat(sources).isNotNull();

        assertThat(sources.getDataSources()).containsKeys("derby");
        assertThat(sources.getDataSource("derby")).isNotNull();

        sources.getConnection("derby").createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection("derby").createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection("derby").createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection("derby").createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection("derby").createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2).trim()).isEqualTo("Caribou");
        results.close();


        // Check the Data Source service
        Collection<ServiceReference<DataSource>> refs = context.getServiceReferences(DataSource.class,
                "(datasource.name=derby)");
        assertThat(refs).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    public void testH2Memory() throws SQLException, InvalidSyntaxException {
        assertThat(sources).isNotNull();

        assertThat(sources.getDataSources()).containsKeys("h2mem");
        assertThat(sources.getDataSource("h2mem")).isNotNull();

        sources.getConnection("h2mem").createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection("h2mem").createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection("h2mem").createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection("h2mem").createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection("h2mem").createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2).trim()).isEqualTo("Caribou");
        results.close();


        // Check the Data Source service
        Collection<ServiceReference<DataSource>> refs = context.getServiceReferences(DataSource.class,
                "(datasource.name=h2mem)");
        assertThat(refs).isNotNull().isNotEmpty().hasSize(1);
    }


    @Test
    public void testH2File() throws SQLException, InvalidSyntaxException {
        assertThat(sources).isNotNull();

        assertThat(sources.getDataSources()).containsKeys("h2file");
        assertThat(sources.getDataSource("h2file")).isNotNull();

        sources.getConnection("h2file").createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection("h2file").createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection("h2file").createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection("h2file").createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection("h2file").createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2).trim()).isEqualTo("Caribou");
        results.close();


        // Check the Data Source service
        Collection<ServiceReference<DataSource>> refs = context.getServiceReferences(DataSource.class,
                "(datasource.name=h2file)");
        assertThat(refs).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    public void testHSQLMem() throws SQLException, InvalidSyntaxException {
        assertThat(sources).isNotNull();

        String database = "hsqlmem";
        assertThat(sources.getDataSources()).containsKeys(database);
        assertThat(sources.getDataSource(database)).isNotNull();

        sources.getConnection(database).createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection(database).createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection(database).createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection(database).createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection(database).createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2).trim()).isEqualTo("Caribou");
        results.close();


        // Check the Data Source service
        Collection<ServiceReference<DataSource>> refs = context.getServiceReferences(DataSource.class,
                "(datasource.name=" + database + ")");
        assertThat(refs).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    public void testHSQLFile() throws SQLException, InvalidSyntaxException {
        assertThat(sources).isNotNull();

        assertThat(sources.getDataSources()).containsKeys("hsqlfile");
        assertThat(sources.getDataSource("hsqlfile")).isNotNull();

        sources.getConnection("hsqlfile").createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection("hsqlfile").createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection("hsqlfile").createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection("hsqlfile").createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection("hsqlfile").createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2).trim()).isEqualTo("Caribou");
        results.close();


        // Check the Data Source service
        Collection<ServiceReference<DataSource>> refs = context.getServiceReferences(DataSource.class,
                "(datasource.name=hsqlfile)");
        assertThat(refs).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    public void testSQLite() throws SQLException, InvalidSyntaxException {
        assertThat(sources).isNotNull();

        String database = "sqlite";
        assertThat(sources.getDataSources()).containsKeys(database);
        assertThat(sources.getDataSource(database)).isNotNull();

        sources.getConnection(database).createStatement().execute(Statements.CREATE_TABLE);
        sources.getConnection(database).createStatement().execute(Statements.INSERT_CARIBOU);
        sources.getConnection(database).createStatement().execute(Statements.INSERT_DENVER);
        sources.getConnection(database).createStatement().execute(Statements.INSERT_PHOENIX);

        ResultSet results = sources.getConnection(database).createStatement().executeQuery(Statements.SELECT_WITH_LAT);
        assertThat(results).isNotNull();
        // We have only one result (CARIBOU)
        results.next();
        assertThat(results.getString(2).trim()).isEqualTo("Caribou");
        results.close();

        // Check the Data Source service
        Collection<ServiceReference<DataSource>> refs = context.getServiceReferences(DataSource.class,
                "(datasource.name=" + database + ")");
        assertThat(refs).isNotNull().isNotEmpty().hasSize(1);
    }

}
