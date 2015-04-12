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
package org.wisdom.content.converters;

import java.lang.reflect.*;
import java.util.*;

/**
 * Some utilities functions to extract type and generic metadata.
 */
public class ReflectionHelper {

    private ReflectionHelper() {
        // Avoid direct instantiation.
    }

    /**
     * Gets the map of String to {@link org.wisdom.content.converters.ReflectionHelper.Property}. These properties
     * are extracted from the given class. Properties are identified using the `setX` methods and fields. The map
     * entry are the property name.
     *
     * @param clazz       the class
     * @param genericType the class with generic parameter if any
     * @return the map containing the extracted properties
     */
    public static Map<String, Property> getProperties(Class clazz, Type genericType) {
        Map<String, Property> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // Start by methods - public only, including overridden
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                // it's a setter.
                String name = method.getName().substring("set".length());
                Property property = map.get(name);
                if (property == null) {
                    property = new Property();
                    map.put(name, property);
                }
                property.setter(method);
            }
        }

        // All fields, but will not do anything for existing properties.
        for (Field field : getAllFields(clazz)) {
            String name = field.getName();
            Property property = map.get(name);
            if (property == null) {
                property = new Property();
                property.field(field);
                map.put(name, property);
            }
            // Else the property has already a setter, the setter has to be used.
        }

        return map;
    }

    /**
     * Property structure
     *
     * @see #getProperties(Class, Type)
     */
    public static class Property {

        private Field field;
        private Class classOfProperty;
        private Type genericOfProperty;
        private Method setter;

        private Property() {
            // Avoid external instantiation.
        }

        /**
         * Sets the current property.
         *
         * @param target the object containing the property
         * @param value  the value
         * @throws InvocationTargetException if the property cannot be set because the method has thrown an exception
         * @throws IllegalAccessException    if the property cannot be set because the property is not accessible
         */
        public void set(Object target, Object value) throws InvocationTargetException, IllegalAccessException {
            if (setter != null) {
                setter.invoke(target, value);
            } else {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(target, value);
            }
        }

        /**
         * The class of the property.
         *
         * @return the class
         */
        public Class<?> getClassOfProperty() {
            return classOfProperty;
        }

        /**
         * The generic type of the property.
         *
         * @return the type
         */
        public Type getGenericTypeOfProperty() {
            return genericOfProperty;
        }

        /**
         * Sets this property to use the given setter method.
         *
         * @param setter the method object for the setter.
         */
        public void setter(Method setter) {
            this.setter = setter;
            this.classOfProperty = setter.getParameterTypes()[0];
            this.genericOfProperty = setter.getGenericParameterTypes()[0];
        }

        /**
         * Sets this property to use a given field
         *
         * @param field the field
         */
        public void field(Field field) {
            this.field = field;
            this.classOfProperty = field.getType();
            this.genericOfProperty = field.getGenericType();
        }
    }

    /**
     * Get the list of class-type pairs that represent the type arguments of a
     * {@link ParameterizedType parameterized} input type.
     * <p>
     * For any given {@link ClassTypePair#rawClass() class} part of each pair
     * in the returned list, following rules apply:
     * <ul>
     * <li>If a type argument is a class then the class is returned as raw class.</li>
     * <li>If the type argument is a generic array type and the generic component
     * type is a class then class of the array is returned as raw class.</li>
     * <li>If the type argument is a parameterized type and it's raw type is a
     * class then that class is returned as raw class.</li>
     * </ul>
     * If the {@code type} is not an instance of ParameterizedType an empty
     * list is returned.
     *
     * @param type parameterized type.
     * @return the list of class-type pairs representing the actual type arguments.
     * May be empty, but may never be {@code null}.
     * @throws IllegalArgumentException if any of the generic type arguments is
     *                                  not a class, or a generic array type, or the generic component type
     *                                  of the generic array type is not class, or not a parameterized type
     *                                  with a raw type that is not a class.
     */
    public static List<ClassTypePair> getTypeArgumentAndClass(final Type type) throws IllegalArgumentException {
        final Type[] types = getTypeArguments(type);
        if (types == null) {
            return Collections.emptyList();
        }

        List<ClassTypePair> list = new ArrayList<>();
        for (Type t : types) {
            list.add(new ClassTypePair(erasure(t), t));
        }
        return list;
    }

    /**
     * Get the {@link Class} representation of the given type.
     * <p>
     * This corresponds to the notion of the erasure in JSR-14.
     *
     * @param type type to provide the erasure for.
     * @return the given type's erasure.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> erasure(Type type) {
        return EraserVisitor.ERASER.visit(type);
    }

    /**
     * Get the type arguments for a parameterized type.
     * <p>
     * In case the type is not a {@link ParameterizedType parameterized type},
     * the method returns {@code null}.
     *
     * @param type parameterized type.
     * @return type arguments for a parameterized type, or {@code null} in case the input type is
     * not a parameterized type.
     */
    public static Type[] getTypeArguments(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        return ((ParameterizedType) type).getActualTypeArguments();
    }

    /**
     * Gets the default value as String for the primitive types.
     *
     * @param type the primitive type
     * @return the default value as String
     */
    public static String getPrimitiveDefault(Class type) {
        if (type == Boolean.class) {
            return "false";
        }
        if (type == Character.class) {
            return Character.toString((char) 0);
        }
        return "0";
    }


    /**
     * Gets all fields of the given class and its parents (if any).
     *
     * @param cls the {@link Class} to query
     * @return an array of Fields (possibly empty).
     */
    public static List<Field> getAllFields(Class<?> cls) {
        final List<Field> allFields = new ArrayList<Field>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }


}
