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

import org.wisdom.api.content.ParameterConverter;

/**
 * Converts String to String, that's the easy one.
 */
public final class StringConverter implements ParameterConverter<String> {

    /**
     * The converter.
     */
    public static final StringConverter INSTANCE = new StringConverter();

    private StringConverter() {
        // No direct instantiation
    }

    /**
     * Just returns the given input.
     *
     * @param input the input, can be {@literal null}
     * @return the input
     */
    @Override
    public String fromString(String input) throws IllegalArgumentException {
        return input;
    }


    /**
     * This converter handles String.
     *
     * @return String.class
     */
    @Override
    public Class<String> getType() {
        return String.class;
    }
}
