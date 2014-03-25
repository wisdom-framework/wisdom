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
package org.wisdom.database.jdbc;

/**
 * Created by clement on 15/02/2014.
 */
public class Statements {

    public static final String CREATE_TABLE = "CREATE TABLE STATION \n" +
            "(ID INTEGER PRIMARY KEY, \n" +
            "CITY CHAR(20), \n" +
            "STATE CHAR(2), \n" +
            "LAT_N REAL, \n" +
            "LONG_W REAL)";

    public static final String INSERT_PHOENIX = "INSERT INTO STATION VALUES (13, 'Phoenix', 'AZ', 33, 112)";
    public static final String INSERT_DENVER = "INSERT INTO STATION VALUES (44, 'Denver', 'CO', 40, 105)";
    public static final String INSERT_CARIBOU = "INSERT INTO STATION VALUES (66, 'Caribou', 'ME', 47, 68)";

    public static final String SELECT_WITH_LAT = "SELECT * FROM STATION WHERE LAT_N = 47";


}
