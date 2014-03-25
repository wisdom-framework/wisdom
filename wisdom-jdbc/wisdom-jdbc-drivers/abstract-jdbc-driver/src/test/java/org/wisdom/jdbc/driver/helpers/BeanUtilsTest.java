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
package org.wisdom.jdbc.driver.helpers;

import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Test the behavior of the bean util class.
 */
public class BeanUtilsTest {

    @Test
    public void testProperties() throws SQLException {
        MyDataSourceBean bean = new MyDataSourceBean();

        String url = "http://perdu.com";
        int count = 25;
        long date = 2014l;
        double d = 25.5;
        char c = 'q';
        byte b = 8;
        short s = 25;
        float f = 25.5f;

        BeanUtils.setProperty(bean, "url", url);
        BeanUtils.setProperty(bean, "count", Integer.toString(count));
        BeanUtils.setProperty(bean, "date", Long.toString(date));
        BeanUtils.setProperty(bean, "flag", Boolean.toString(true));
        BeanUtils.setProperty(bean, "d", Double.toString(d));
        BeanUtils.setProperty(bean, "s", Short.toString(s));
        BeanUtils.setProperty(bean, "b", Byte.toString(b));
        BeanUtils.setProperty(bean, "c", Character.toString(c));
        BeanUtils.setProperty(bean, "f", Float.toString(f));

        assertThat(bean.url).isEqualTo(url);
        assertThat(bean.count).isEqualTo(count);
        assertThat(bean.date).isEqualTo(date);
        assertThat(bean.flag).isTrue();
        assertThat(bean.d).isEqualTo(d);
        assertThat(bean.c).isEqualTo(c);
        assertThat(bean.s).isEqualTo(s);
        assertThat(bean.b).isEqualTo(b);
        assertThat(bean.f).isEqualTo(f);
    }

    @Test
    public void testNullProperty() throws SQLException {
        MyDataSourceBean bean = new MyDataSourceBean();
        BeanUtils.setProperty(bean, "url", null);
        assertThat(bean.url).isNull();
    }

    @Test(expected = SQLException.class)
    public void testMissingProperty() throws SQLException {
        MyDataSourceBean bean = new MyDataSourceBean();
        BeanUtils.setProperty(bean, "missing", "");
    }

    @Test
    public void testInvalidPropertyTypes() throws SQLException {
        MyDataSourceBean bean = new MyDataSourceBean();
        try {
            BeanUtils.setProperty(bean, "count", "this is not a valid integer value");
            fail("SQL Exception expected");
        } catch (SQLException e) {
            // OK
        }

        try {
            BeanUtils.setProperty(bean, "date", "this is not a valid long value");
            fail("SQL Exception expected");
        } catch (SQLException e) {
            // OK
        }

        try {
            BeanUtils.setProperty(bean, "d", "this is not a valid double value");
            fail("SQL Exception expected");
        } catch (SQLException e) {
            // OK
        }

        try {
            BeanUtils.setProperty(bean, "c", "this is not a valid character value");
            fail("SQL Exception expected");
        } catch (SQLException e) {
            // OK
        }

        try {
            BeanUtils.setProperty(bean, "s", "this is not a valid short value");
            fail("SQL Exception expected");
        } catch (SQLException e) {
            // OK
        }

        try {
            BeanUtils.setProperty(bean, "b", "this is not a valid byte value");
            fail("SQL Exception expected");
        } catch (SQLException e) {
            // OK
        }

        try {
            BeanUtils.setProperty(bean, "f", "this is not a valid float value");
            fail("SQL Exception expected");
        } catch (SQLException e) {
            // OK
        }

        BeanUtils.setProperty(bean, "flag", "this is not a valid boolean value");
        assertThat(bean.flag).isFalse();
    }
}
