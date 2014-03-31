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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Configures an object based on the given properties.
 */
public class BeanUtils {

    private BeanUtils() {
        // Avoid direct instantiation
    }

    /**
     * Tries to set the property 'name' to `value` in the given object. This assignation is made using a
     * <em>setter</em> method discovered and invoked using reflection.
     * @param object the object
     * @param name the property name
     * @param value the value
     * @throws SQLException if the property cannot be set. This happens if there are no setter for the given property
     * in the object or if the value cannot be wrapped to the type of the setter parameter.
     */
    public static void setProperty(Object object, String name, String value)
            throws SQLException {
        Class<?> type = object.getClass();

        PropertyDescriptor[] descriptors;
        try {
            descriptors = Introspector.getBeanInfo(type)
                    .getPropertyDescriptors();
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
        List<String> names = new ArrayList<>();

        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getWriteMethod() == null) {
                continue;
            }

            if (descriptor.getName().equals(name)) {
                Method method = descriptor.getWriteMethod();
                Class<?> paramType = method.getParameterTypes()[0];
                Object param = toBasicType(value, paramType.getName());

                try {
                    method.invoke(object, param);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }
                return;
            }

            names.add(descriptor.getName());
        }
        throw new SQLException("No such property: " + name +
                ", exists.  Writable properties are: " + names);
    }

    /**
     * Transforms the given value to an instance of the given type.
     * @param value the value
     * @param type the type
     * @return an instance of the type having the wrapped value
     * @throws SQLException if the conversion is not possible.
     */
    public static Object toBasicType(String value, String type) throws SQLException {

        // Early return from first "if" condition that evaluates to true

        if (value == null) {
            return null;
        }

        if (type == null || type.equals(String.class.getName())) {
            return value;
        }

        if (type.equals(Integer.class.getName()) || type.equals(int.class.getName())) {
            try { return Integer.valueOf(value); }
            catch (NumberFormatException e) {
                throwSQLException(e, "Integer", value);
            }
        }

        if (type.equals(Float.class.getName()) || type.equals(float.class.getName())) {
            try { return Float.valueOf(value); }
            catch (NumberFormatException e) {
                throwSQLException(e, "Float", value);
            }
        }

        if (type.equals(Long.class.getName()) || type.equals(long.class.getName())) {
            try { return Long.valueOf(value); }
            catch (NumberFormatException e) {
                throwSQLException(e, "Long", value);
            }
        }

        if (type.equals(Double.class.getName()) || type.equals(double.class.getName())) {
            try { return Double.valueOf(value); }
            catch (NumberFormatException e) {
                throwSQLException(e, "Double", value);
            }
        }

        if (type.equals(Character.class.getName()) || type.equals(char.class.getName())) {
            if (value.length() != 1) {
                throw new SQLException("Invalid Character value: " + value);
            }
            return value.charAt(0);
        }

        if (type.equals(Byte.class.getName()) || type.equals(byte.class.getName())) {
            try { return Byte.valueOf(value); }
            catch (NumberFormatException e) {
                throwSQLException(e, "Byte", value);
            }
        }

        if (type.equals(Short.class.getName()) || type.equals(short.class.getName())) {
            try { return Short.valueOf(value); }
            catch (NumberFormatException e) {
                throwSQLException(e, "Short", value);
            }
        }

        // Will be "false" if not in correct format...
        if (type.equals(Boolean.class.getName()) || type.equals(boolean.class.getName())) {
            return Boolean.valueOf(value);
        }

        throw new SQLException("Unrecognized property type: " + type);
    }

    /**
     * An helper method to build and throw a SQL Exception when a property cannot be set.
     * @param cause the cause
     * @param theType the type of the property
     * @param value the value of the property
     * @throws SQLException the SQL Exception
     */
    public static void throwSQLException(Exception cause, String theType, String value)
            throws SQLException {
        throw new SQLException("Invalid " + theType + " value: " + value, cause);
    }
}
