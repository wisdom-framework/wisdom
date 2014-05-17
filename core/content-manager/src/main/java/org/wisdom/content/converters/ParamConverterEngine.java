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

import com.google.common.primitives.Primitives;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterConverters;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Content Engine.
 */
@Component
@Provides
@Instantiate(name = "ParameterConverterEngine")
public class ParamConverterEngine implements ParameterConverters {

    @Requires(specification = ParameterConverter.class, optional = true)
    List<ParameterConverter> converters;

    public ParamConverterEngine() {
        // The constructor used by iPOJO.
    }

    /**
     * Constructor used for testing purpose only.
     *
     * @param list the list of converter.
     */
    public ParamConverterEngine(List<ParameterConverter> list) {
        converters = list;
    }

    @Override
    public <T> T convertValue(String input, Class<T> rawType, Type type, String defaultValue) throws IllegalArgumentException {
        if (rawType.isArray()) {
            List<String> args = getMultipleValues(input, defaultValue);
            return createArray(args, rawType.getComponentType());
        } else if (Collection.class.isAssignableFrom(rawType)) {
            List<String> args = getMultipleValues(input, defaultValue);
            return createCollection(args, rawType, type);
        } else {
            return convertSingleValue(input, rawType, defaultValue);
        }
    }

    private List<String> getMultipleValues(String input, String defaultValue) {
        if (input == null && defaultValue == null) {
            return null;
        }
        if (input == null) {
            input = defaultValue;
        }
        String[] segments = input.split(",");
        List<String> values = new ArrayList<>();
        for (String s : segments) {
            String v = s.trim();
            if (!v.isEmpty()) {
                values.add(v);
            }
        }
        return values;
    }

    /**
     * Creates an instance of T from the given input. Unlike {@link #convertValue(String, Class,
     * java.lang.reflect.Type, String)}, this method support multi-value parameters.
     *
     * @param input        the input Strings, may be {@literal null} or empty
     * @param rawType      the target class
     * @param type         the type representation of the raw type, may contains metadata about generics
     * @param defaultValue the default value if any
     * @return the created object
     * @throws IllegalArgumentException if there are no converter available from the type T at the moment
     */
    @Override
    public <T> T convertValues(Collection<String> input, Class<T> rawType, Type type, String defaultValue) throws IllegalArgumentException {
        if (rawType.isArray()) {
            if (input == null) {
                input = getMultipleValues(defaultValue, null);
            }
            return createArray(input, rawType.getComponentType());
        } else if (Collection.class.isAssignableFrom(rawType)) {
            if (input == null) {
                input = getMultipleValues(defaultValue, null);
            }
            return createCollection(input, rawType, type);
        } else {
            return convertSingleValue(input, rawType, defaultValue);
        }
    }

    private <T> T createCollection(Collection<String> input, Class<T> rawType, Type type) {
        // Get the generic type of the list
        // If none default to String
        final List<ClassTypePair> ctps = ReflectionHelper.getTypeArgumentAndClass(type);
        ClassTypePair ctp = (ctps.size() == 1) ? ctps.get(0) : null;

        if (ctp == null || ctp.rawClass() == String.class) {
            return createCollectionWithConverter(input, rawType, StringConverter.INSTANCE);
        } else {
            ParameterConverter converter = getConverter(ctp.rawClass());
            // On Java 8 we cannot use 'cast' here, I don't really understand why.
            //noinspection unchecked
            return (T) createCollectionWithConverter(input, rawType, converter);
        }
    }

    private <T, A> T createCollectionWithConverter(Collection<String> input, Class<T> type,
                                                   ParameterConverter<A> converter) {
        Collection<A> collection;
        if (type == Collection.class || List.class.isAssignableFrom(type)) {
            if (input == null) {
                return type.cast(Collections.emptyList());
            }
            collection = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(type)) {
            if (input == null) {
                return type.cast(Collections.emptySet());
            }
            collection = new LinkedHashSet<>();
        } else {
            throw new IllegalArgumentException("Not supported collection type " + type.getName());
        }

        for (String v : input) {
            collection.add(converter.fromString(v));
        }

        return type.cast(collection);
    }

    private <T> T createArray(Collection<String> input, Class<?> componentType) {
        if (input == null) {
            //noinspection unchecked
            return (T) Array.newInstance(componentType, 0);
        }

        Class<?> theType = componentType;
        if (componentType.isPrimitive()) {
            theType = Primitives.wrap(componentType);
        }

        ParameterConverter converter = getConverter(theType);

        List<Object> list = new ArrayList<>();
        for (String v : input) {
            list.add(converter.fromString(v));
        }
        // We cannot use the toArray method as the the type does not match (toArray would produce an object[]).
        Object array = Array.newInstance(componentType, list.size());
        int i = 0;
        for (Object o : list) {
            Array.set(array, i, o);
            i++;
        }

        //noinspection unchecked
        return (T) array;
    }

    private <T> T convertSingleValue(String input, Class<T> type, String defaultValue) {
        if (type.isPrimitive()) {
            type = Primitives.wrap(type);
            if (input == null  && defaultValue == null) {
                defaultValue = ReflectionHelper.getPrimitiveDefault(type);
            }
        }

        ParameterConverter<T> converter = getConverter(type);
        if (input != null) {
            return converter.fromString(input);
        } else {
            return converter.fromString(defaultValue);
        }
    }


    private <T> T convertSingleValue(Collection<String> input, Class<T> type, String defaultValue) {
        if (input == null || input.isEmpty()) {
            return convertSingleValue((String) null, type, defaultValue);
        } else {
            String v = input.iterator().next();
            return convertSingleValue(v, type, defaultValue);
        }
    }

    /**
     * Searches a suitable converter to convert String to the given type.
     *
     * @param type the target type
     * @param <T>  the class
     * @return the parameter converter able to creates instances of the target type from String representations.
     * @throws java.util.NoSuchElementException if no converter can be found
     */
    @SuppressWarnings("unchecked")
    private <T> ParameterConverter<T> getConverter(Class<T> type) {
        // check for String first
        if (type == String.class) {
            return (ParameterConverter<T>) StringConverter.INSTANCE;
        }

        // Search for exposed converters.
        for (ParameterConverter pc : converters) {
            if (pc.getType().equals(type)) {
                return pc;
            }
        }

        // Boolean has a special case as they support other form of "truth" such as "yes", "on", "1"...
        if (type == Boolean.class) {
            return (ParameterConverter<T>) BooleanConverter.INSTANCE;
        }

        // None of them are there, try default converters in the following order:
        // 1. constructor
        // 2. valueOf
        // 3. from
        // 4. fromString
        ParameterConverter<T> converter = ConstructorBasedConverter.getIfEligible(type);
        if (converter != null) {
            return converter;
        }
        converter = ValueOfBasedConverter.getIfEligible(type);
        if (converter != null) {
            return converter;
        }
        converter = FromBasedConverter.getIfEligible(type);
        if (converter != null) {
            return converter;
        }
        converter = FromStringBasedConverter.getIfEligible(type);
        if (converter != null) {
            return converter;
        }

        // Unlike other primitive type, characters cannot be created using the 'valueOf' method,
        // so we need a specific converter. As creating characters is quite rare, this must be the last check.
        if (type == Character.class) {
            return (ParameterConverter<T>) CharacterConverter.INSTANCE;
        }

        // running out of converters...
        throw new NoSuchElementException("Cannot find a converter able to create instance of " + type.getName());
    }

}
