package org.wisdom.database.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the data source manager inside Wisdom.
 */
public class DataSourcesIT extends WisdomTest {

    @Inject
    DataSources sources;

    @Test
    public void testExposition() throws SQLException {
         assertThat(sources).isNotNull();

        assertThat(sources.getDataSources()).hasSize(1);
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
    }

}
