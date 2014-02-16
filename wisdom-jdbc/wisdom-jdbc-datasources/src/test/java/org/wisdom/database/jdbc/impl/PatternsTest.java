package org.wisdom.database.jdbc.impl;

import com.jolbox.bonecp.BoneCPDataSource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the parsing of JDBC urls.
 */
public class PatternsTest {

    @Test
    public void testPostgresOnLocalhost() {
        String url = "jdbc:postgresql://localhost/test";

        BoneCPDataSource source = new BoneCPDataSource();

        assertThat(Patterns.populate(source, url, false)).isFalse();
        assertThat(source.getConfig().getJdbcUrl()).isEqualTo(url);
        assertThat(source.getConfig().getUsername()).isNull();
    }

    @Test
    public void testWithMySQLWithoutAttributes() {
        String url = "mysql://clement:secret@localhost/test";

        BoneCPDataSource source = new BoneCPDataSource();
        assertThat(Patterns.populate(source, url, false)).isTrue();

        assertThat(source.getConfig().getJdbcUrl())
                .isEqualTo(
                        "jdbc:mysql://localhost/test?useUnicode=yes&characterEncoding=UTF-8&connectionCollation" +
                                "=utf8_general_ci");
        assertThat(source.getConfig().getUsername()).isEqualTo("clement");
        assertThat(source.getConfig().getPassword()).isEqualTo("secret");
    }

    @Test
    public void testWithMySQLWithAttributes() {
        String url = "mysql://clement:secret@localhost:3306/wisdom?useUnicode=true&characterEncoding=utf8";

        BoneCPDataSource source = new BoneCPDataSource();
        assertThat(Patterns.populate(source, url, false)).isTrue();

        assertThat(source.getConfig().getJdbcUrl())
                .isEqualTo(
                        "jdbc:mysql://localhost:3306/wisdom?useUnicode=true&characterEncoding=utf8");
        assertThat(source.getConfig().getUsername()).isEqualTo("clement");
        assertThat(source.getConfig().getPassword()).isEqualTo("secret");
    }

    @Test
    public void testH2InDevMode() {
        String url = "jdbc:h2:mem:h2-mem-it";

        BoneCPDataSource source = new BoneCPDataSource();

        assertThat(Patterns.populate(source, url, true)).isTrue();
        assertThat(source.getConfig().getJdbcUrl()).isEqualTo(url + ";DB_CLOSE_DELAY=-1");
        assertThat(source.getConfig().getUsername()).isNull();
    }

    @Test
    public void testH2InProdMode() {
        String url = "jdbc:h2:mem:h2-mem-it";

        BoneCPDataSource source = new BoneCPDataSource();

        assertThat(Patterns.populate(source, url, false)).isTrue();
        assertThat(source.getConfig().getJdbcUrl()).isEqualTo(url);
        assertThat(source.getConfig().getUsername()).isNull();
    }

}
