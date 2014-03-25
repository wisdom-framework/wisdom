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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class responsible for parsing the given jdbc url and populating the data source with data found in the url. If
 * also automatically extend the url with 'good practices'.
 */
public class Patterns {

    public static final Pattern MYSQL_URL = Pattern.compile("^mysql://([a-zA-Z0-9_]+):([^@]+)@([^/]+)/([^\\s]+)$");
    public static final Pattern MYSQL_CUSTOM_PROPERTIES = Pattern.compile(".*\\?(.*)");
    public static final Pattern H2_URL = Pattern.compile("^jdbc:h2:mem:.+");
    public static final String DEFAULT_MYSQL_PROPERTIES = "?useUnicode=yes&characterEncoding=UTF-8&connectionCollation=utf8_general_ci";

    /**
     * Checks if the given url is matching one of the known pattern (MySQL and H2),
     * if so try to extract information from the url and populate the data source.
     * <p/>
     * In all case, this method set the data source url, even if none known pattern match.
     *
     * @param datasource the data source to populate
     * @param url        the jdbc url
     * @param isDev      a boolean indicating if the wisdom server is running in 'dev' mode.
     * @return {@literal true} if the data soruce was populated, {@literal false} otherwise
     */
    public static boolean populate(BoneCPDataSource datasource, String url, boolean isDev) {
        Matcher matcher = MYSQL_URL.matcher(url);
        if (matcher.matches()) {
            populateForMySQLFull(matcher, url, datasource);
            return true;
        }

        matcher = H2_URL.matcher(url);
        if (matcher.matches()) {
            populateForH2(url, datasource, isDev);
            return true;
        }

        // Set the url to the raw url.
        datasource.setJdbcUrl(url);

        return false;
    }

    private static void populateForMySQLFull(Matcher matcher, String url, BoneCPDataSource datasource) {
        String username = matcher.group(1);
        String password = matcher.group(2);
        String host = matcher.group(3);
        String db = matcher.group(4);

        String defaultProperties = DEFAULT_MYSQL_PROPERTIES;
        if (MYSQL_CUSTOM_PROPERTIES.matcher(url).find()) {
            defaultProperties = "";
        }
        datasource.setJdbcUrl(String.format("jdbc:mysql://%s/%s", host, db + defaultProperties));
        datasource.setUsername(username);
        datasource.setPassword(password);
    }

    private static void populateForH2(String url, BoneCPDataSource datasource,
                                      boolean isDev) {
        if (!url.contains("DB_CLOSE_DELAY") && isDev) {
            datasource.setJdbcUrl(url + ";DB_CLOSE_DELAY=-1");
        } else {
            datasource.setJdbcUrl(url);
        }
    }
}
