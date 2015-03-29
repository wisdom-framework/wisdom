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

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.BodyParser;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.content.converters.ReflectionHelper;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
@Provides
@Instantiate
public class BodyParserForm implements BodyParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyParser.class);
    private static final String ERROR_KEY = "Error parsing incoming form data for key ";
    private static final String ERROR_AND = " and value ";

    @Requires
    ParameterFactories converters;

    /**
     * Creates a object of class T from a form sent in the request.
     *
     * @param context     The context
     * @param classOfT    The class we expect
     * @param genericType the generic type (ignored)
     * @param <T>         the class ot the object to build
     * @return the object, {@code null} if the object cannot be built.
     */
    @Override
    public <T> T invoke(Context context, Class<T> classOfT, Type genericType) {
        T t;
        try {
            t = classOfT.newInstance();
        } catch (Exception e) {
            LOGGER.error("Failed to create a new instance of {}", classOfT, e);
            return null;
        }

        Map<String, ReflectionHelper.Property> properties = ReflectionHelper.getProperties(classOfT, genericType);

        // 1) Query parameters
        for (Entry<String, List<String>> ent : context.parameters().entrySet()) {
            try {
                ReflectionHelper.Property property = properties.get(ent.getKey());
                if (property != null) {
                    Object value = converters.convertValues(ent.getValue(), property.getClassOfProperty(),
                            property.getGenericTypeOfProperty(),
                            null);
                    property.set(t, value);
                }
            } catch (Exception e) {
                LOGGER.warn(ERROR_KEY + ent.getKey() + ERROR_AND + ent.getValue(), e);
            }
        }
        // 2) Path parameters
        final Map<String, String> fromPath = context.route().getPathParametersEncoded(context.request().uri());
        for (Entry<String, String> ent : fromPath
                .entrySet()) {
            try {
                ReflectionHelper.Property property = properties.get(ent.getKey());
                if (property != null) {
                    Object value = converters.convertValue(ent.getValue(), property.getClassOfProperty(),
                            property.getGenericTypeOfProperty(), null);
                    property.set(t, value);
                }
            } catch (Exception e) {
                // Path parameter are rarely used in form, so, set the log level to 'debug'.
                LOGGER.debug(ERROR_KEY + ent.getKey() + ERROR_AND + ent.getValue(), e);
            }
        }

        // 3) Forms.
        if (context.form() == null || context.form().isEmpty()) {
            return t;
        }
        for (Entry<String, List<String>> ent : context.form().entrySet()) {
            try {
                ReflectionHelper.Property property = properties.get(ent.getKey());
                if (property != null) {
                    Object value = converters.convertValues(ent.getValue(), property.getClassOfProperty(),
                            property.getGenericTypeOfProperty(), null);
                    property.set(t, value);
                }
            } catch (Exception e) {
                LOGGER.warn(ERROR_KEY + ent.getKey() + ERROR_AND + ent.getValue(), e);
            }
        }

        // 4) File Items.
        if (context.files() == null || context.files().isEmpty()) {
            return t;
        }
        for (FileItem item : context.files()) {
            try {
                ReflectionHelper.Property property = properties.get(item.field());
                if (property != null) {
                    if (InputStream.class.isAssignableFrom(property.getClassOfProperty())) {
                        property.set(t, item.stream());
                    } else if (FileItem.class.isAssignableFrom(property.getClassOfProperty())) {
                        property.set(t, item);
                    } else if (property.getClassOfProperty().isArray()
                            && property.getClassOfProperty().getComponentType().equals(Byte.TYPE)) {
                        property.set(t, item.bytes());
                    }
                }
            } catch (Exception e) {
                LOGGER.warn(ERROR_KEY + item.field() + ERROR_AND + item, e);
            }
        }

        return t;
    }

    /**
     * Creates a object of class T from a form sent in the request.
     *
     * @param context  The context
     * @param classOfT The class we expect
     * @param <T>      the class ot the object to build
     * @return the object, {@code null} if the object cannot be built.
     */
    @Override
    public <T> T invoke(Context context, Class<T> classOfT) {
        return invoke(context, classOfT, null);
    }

    /**
     * Unsupported operation.
     *
     * @param bytes    the content
     * @param classOfT The class we expect
     * @param <T>      the class
     * @return nothing as this method is not supported
     */
    @Override
    public <T> T invoke(byte[] bytes, Class<T> classOfT) {
        throw new UnsupportedOperationException("Cannot bind a raw byte[] to a form object");
    }

    /**
     * @return a list containing {@code application/x-www-form-urlencoded} and {@code multipart/form}.
     */
    public List<String> getContentTypes() {
        return ImmutableList.of(MimeTypes.FORM, MimeTypes.MULTIPART);
    }
}
