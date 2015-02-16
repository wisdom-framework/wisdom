/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.i18n;

import com.google.common.base.Strings;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.Locale;

/**
 * Register a {@link ParameterConverter} able to convert String to {@link Locale} object.
 */
@Service
public class LocaleParameterConverter implements ParameterConverter<Locale> {
    /**
     * Converts the given String to a {@link Locale} object using the {@link Locale#forLanguageTag(String)} method.
     *
     * @param input the input, can be {@literal null}
     * @return the created locale, empty locale ({@code "" "" ""}) if the given input is {@code null} or empty.
     * @throws IllegalArgumentException if the instance of T cannot be created from the input.
     */
    @Override
    public Locale fromString(String input) throws IllegalArgumentException {
        if (!Strings.isNullOrEmpty(input)) {
            return Locale.forLanguageTag(input.replace("_", "-"));
        }
        return InternationalizationService.DEFAULT_LOCALE;
    }

    /**
     * Gets the type created by this converter.
     *
     * @return the {@link Locale} class
     */
    @Override
    public Class<Locale> getType() {
        return Locale.class;
    }
}
