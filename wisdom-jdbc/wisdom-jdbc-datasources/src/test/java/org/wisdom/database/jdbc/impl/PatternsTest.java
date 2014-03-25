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
