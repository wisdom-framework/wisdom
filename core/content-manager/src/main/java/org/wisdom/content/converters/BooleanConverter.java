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

import com.google.common.collect.ImmutableSet;
import org.wisdom.api.content.ParameterConverter;

import java.util.Set;

/**
 * A converter for boolean. This converter considered as 'true' : "true", "on", "1",
 * "yes". All other values are considered as 'false' (as a consequence, 'null' is considered as 'false').
 */
public final class BooleanConverter implements ParameterConverter<Boolean> {

    /**
     * The converter.
     */
    public static final BooleanConverter INSTANCE = new BooleanConverter();

    private BooleanConverter() {
        // No direct instantiation
    }

    /**
     * The set of values considered as 'true'.
     */
    private static Set<String> TRUE = ImmutableSet.of("true", "yes", "on", "1");

    /**
     * Creates the boolean value from the given String. If the given String does not match one of the 'true' value,
     * {@code false} is returned.
     *
     * @param value the value
     * @return the boolean object
     */
    @Override
    public Boolean fromString(String value) {
        return value != null && TRUE.contains(value.toLowerCase());
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }
}
