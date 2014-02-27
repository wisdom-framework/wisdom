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
