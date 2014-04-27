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
package org.wisdom.content.bodyparsers;

import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities to convert types.
 */
public class Converters {

    private static final Logger logger = LoggerFactory.getLogger(Converters.class);

    private static final Map<String, Method> CONVERTERS = new HashMap<>();

    static {
        Method[] methods = Converter.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0] == String.class) {

                CONVERTERS.put(method.getReturnType().getName(), method);
            }
        }
    }

    /**
     * Convert value to class type value.
     * <p/>
     * If something goes wrong it returns null.
     *
     * @param from string value
     * @param to   type of the class
     * @return class type value or null if something goes wrong.
     */
    public static <T> T convert(String from, Class<T> to) {

        Class<T> toAsNonPrimitiveType;

        if (from == null) {
            return null;
        }

        T t = null;

        toAsNonPrimitiveType = Primitives.wrap(to);

        if (toAsNonPrimitiveType.isAssignableFrom(from.getClass())) {
            return toAsNonPrimitiveType.cast(from);
        }

        Method converter = CONVERTERS.get(toAsNonPrimitiveType.getName());

        if (converter == null) {

            logger.error(
                    "No converter found to convert {}. "
                            + "Returning null. "
                            + "You may want to extend the class.", toAsNonPrimitiveType
            );

        } else {

            try {

                t = toAsNonPrimitiveType.cast(converter.invoke(toAsNonPrimitiveType, from));

            } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException ex) {

                logger.error(
                        "Cannot convert from "
                                + from.getClass().getName() + " to " + toAsNonPrimitiveType.getName()
                                + ". Conversion failed with " + ex.getMessage(), ex
                );
            }

        }

        return t;

    }

    private static class Converter {

        public static Integer toInteger(String value) {
            return Integer.valueOf(value);
        }

        public static Long toLong(String value) {
            return Long.valueOf(value);
        }

        public static Float toFloat(String value) {
            return Float.valueOf(value);
        }

        public static Double toDouble(String value) {
            return Double.valueOf(value);
        }

        public static Boolean toBoolean(String value) {
            return Boolean.valueOf(value);
        }

        public static Byte toByte(String value) {
            return Byte.valueOf(value);
        }

        public static Short toShort(String value) {
            return Short.valueOf(value);
        }

        public static Character toCharacter(String value) {

            if (value.length() > 0) {
                return value.charAt(0);
            } else {
                return null;
            }

        }
    }
}
