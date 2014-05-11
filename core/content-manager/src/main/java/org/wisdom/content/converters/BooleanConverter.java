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

import com.google.common.collect.ImmutableList;
import org.wisdom.api.content.ParameterConverter;

import java.util.List;

/**
 * A converter for boolean. This converter considered as 'true' : "true", "on", "1",
 * "yes". All other values are considered as 'false' (as a consequence, 'null' is considered as 'false').
 */
public class BooleanConverter implements ParameterConverter<Boolean> {

    /**
     * The converter.
     */
    public static final BooleanConverter INSTANCE = new BooleanConverter();

    private BooleanConverter() {
        // No direct instantiation
    }

    private static List<String> TRUE = ImmutableList.of("true", "yes", "on", "1");


    @Override
    public Boolean fromString(String value) throws IllegalArgumentException {
        return (value != null && TRUE.contains(value.toLowerCase()));
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }
}
