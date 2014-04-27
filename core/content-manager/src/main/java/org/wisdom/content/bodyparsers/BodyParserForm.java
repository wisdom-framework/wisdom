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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.BodyParser;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

@Component
@Provides
@Instantiate
public class BodyParserForm implements BodyParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyParser.class);
    private static final String ERROR_KEY = "Error parsing incoming form data for key ";
    private static final String ERROR_AND = " and value ";

    @Override
    public <T> T invoke(Context context, Class<T> classOfT) {
        T t = null;
        try {
            t = classOfT.newInstance();
        } catch (Exception e) {
            LOGGER.error("can't newInstance class " + classOfT.getName(), e);
            return null;
        }
        for (Entry<String, List<String>> ent : context.parameters().entrySet()) {
            try {
                Field field = classOfT.getDeclaredField(ent.getKey());
                field.setAccessible(true);
                field.set(t, ent.getValue().get(0));
            } catch (Exception e) {
                LOGGER.warn(
                        ERROR_KEY + ent.getKey()
                                + ERROR_AND + ent.getValue(), e
                );
            }
        }

        if (context.attributes() == null) {
            return t;
        }
        for (Entry<String, List<String>> ent : context.attributes().entrySet()) {
            try {
                Field field = classOfT.getDeclaredField(ent.getKey());
                field.setAccessible(true);

                if (field.getType().equals(List.class) || field.getType().equals(Collection.class)) {
                    field.set(t, ent.getValue());
                } else if (ent.getValue() != null && !ent.getValue().isEmpty()) {
                    Object convertedValue = Converters.convert(ent.getValue().get(0), field.getType());
                    field.set(t, convertedValue);
                }

            } catch (NoSuchFieldException e) {
                LOGGER.warn("No member in {} to be bound with attribute {}={}", classOfT.getName(), ent.getKey(),
                        ent.getValue(), e);
            } catch (Exception e) {
                LOGGER.warn(
                        ERROR_KEY + ent.getKey()
                                + ERROR_AND + ent.getValue(), e
                );
            }
        }
        return t;
    }

    @Override
    public <T> T invoke(byte[] bytes, Class<T> classOfT) {
        throw new UnsupportedOperationException("Cannot bind a raw byte[] to a form object");
    }

    public List<String> getContentTypes() {
        return ImmutableList.of(MimeTypes.FORM);
    }
}
