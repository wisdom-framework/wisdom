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
package org.wisdom.database.jdbc.it;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.wisdom.database.jdbc.Statements;
import org.wisdom.database.jdbc.service.DataSources;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.File;
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

    @Before
    public void setup() {
        File db = new File("target/db");
        db.mkdirs();
    }

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
        Collection<ServiceReference<DataSourceFactory>> factories = context.getServiceReferences(DataSourceFactory
                .class, null);
        System.out.println("Factories:");
        for (ServiceReference<DataSourceFactory> factory : factories) {
            System.out.println("\t driver: " + factory.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
        }


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
