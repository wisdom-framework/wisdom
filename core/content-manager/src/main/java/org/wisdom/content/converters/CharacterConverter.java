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
 * A converter for character. Unlike other primitive types, characters cannot be created using 'valueOf'. Notice that
 * only input having a length of 1 can be converted to characters. Other inputs are rejected.
 */
public final class CharacterConverter implements ParameterConverter<Character> {

    /**
     * The converter.
     */
    public static final CharacterConverter INSTANCE = new CharacterConverter();

    private CharacterConverter() {
        // No direct instantiation
    }

    @Override
    public Character fromString(String input) throws IllegalArgumentException {
        if (input == null) {
            throw new NullPointerException("input must not be null");
        }

        if (input.length() != 1) {
            throw new IllegalArgumentException("The input string \"" + input + "\" cannot be converted to a " +
                    "character. The input's length must be 1");
        }

        return input.toCharArray()[0];

    }

    @Override
    public Class<Character> getType() {
        return Character.class;
    }
}
