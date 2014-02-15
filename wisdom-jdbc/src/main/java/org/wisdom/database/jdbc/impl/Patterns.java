package org.wisdom.database.jdbc.impl;

import java.util.regex.Pattern;

/**
 * Created by clement on 15/02/2014.
 */
public class Patterns {

    Pattern PostgresFullUrl = Pattern.compile("^postgres://([a-zA-Z0-9_]+):([^@]+)@([^/]+)/([^\\s]+)$");
    Pattern MysqlFullUrl = Pattern.compile("^mysql://([a-zA-Z0-9_]+):([^@]+)@([^/]+)/([^\\s]+)$");
    Pattern MysqlCustomProperties = Pattern.compile(".*\\?(.*)");
    Pattern H2DefaultUrl = Pattern.compile("^jdbc:h2:mem:.+");
}
