package org.wisdom.database.jdbc;

import org.junit.Test;
import org.wisdom.database.jdbc.utils.ClassLoaders;
import org.wisdom.test.parents.WisdomTest;

import java.sql.Driver;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the loading and instantiation of JDBC drivers.
 */
public class DriverIT extends WisdomTest {

    @Test
    public void testMySQL() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        String driverClassName = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/wisdom";

        Class clazz = ClassLoaders.loadClass(context, driverClassName);
        assertThat(clazz).isNotNull();

        Driver driver = (Driver) clazz.newInstance();
        assertThat(driver).isNotNull();
        System.out.println("JDBC Driver " + driverClassName + " version " + driver.getMajorVersion() + "." + driver
                .getMinorVersion());
        assertThat(driver.acceptsURL(url)).isTrue();


        // Test with default attributes
        url = "jdbc:mysql://localhost/test?useUnicode=yes&characterEncoding=UTF-8&connectionCollation=utf8_general_ci";
        assertThat(driver.acceptsURL(url)).isTrue();
    }

    @Test
    public void testPostgreSQL() throws ClassNotFoundException, IllegalAccessException, InstantiationException,
            SQLException {
        String driverClassName = "org.postgresql.Driver";
        String url = "jdbc:postgresql:wisdom";

        Class clazz = ClassLoaders.loadClass(context, driverClassName);
        assertThat(clazz).isNotNull();

        Driver driver = (Driver) clazz.newInstance();
        assertThat(driver).isNotNull();
        System.out.println("JDBC Driver " + driverClassName + " version " + driver.getMajorVersion() + "." + driver
                .getMinorVersion());
        assertThat(driver.acceptsURL(url)).isTrue();
    }

}
