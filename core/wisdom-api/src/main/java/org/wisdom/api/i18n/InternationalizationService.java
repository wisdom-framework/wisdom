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
package org.wisdom.api.i18n;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A service to retrieve internationalized messages.
 */
public interface InternationalizationService {

    /**
     * The default locale object.
     */
    Locale DEFAULT_LOCALE = new Locale("", "", "");

    /**
     * The property name used to configure the value of the default locale in the application.conf file.
     * By default, it uses English.
     * <p>
     * Property replaced by {@link #APPLICATION_DEFAULT_LOCALE}.
     * @deprecated use {@link #APPLICATION_DEFAULT_LOCALE} instead.
     */
    @Deprecated
    String APPLICATION_DEFAULT_LOCALE_OLD = "application.default.locale";

    /**
     * The property name used to configure the value of the default locale in the application.conf file.
     * By default, it uses English.
     */
    String APPLICATION_DEFAULT_LOCALE = "application.locale";

    /**
     * Retrieves the set of resource bundles handled by the system.
     *
     * @return the set of resource bundle, empty if none.
     */
    Collection<ResourceBundle> bundles();

    /**
     * Retrieves the default locale as configured by the application.
     *
     * @return the default locale
     */
    Locale defaultLocale();

    /**
     * Retrieves the set of resource bundles handled by the system providing messages for the given locale. The set
     * of resource bundle does not contain the default message. A specific call using {@link #defaultLocale()} must
     * be done.
     *
     * @param locale the locale
     * @return the set of resource bundle, empty if none.
     */
    Collection<ResourceBundle> bundles(Locale locale);

    /**
     * Gets the message identified by `key` for the given locale. The message can be parameterized using `args`,
     * applied to the message using  {@link java.text.MessageFormat}. If the message is not provided for the given
     * locale, {@literal null} is returned
     *
     * @param locale the locale
     * @param key    the key
     * @param args   the arguments (optional)
     * @return the formatted internationalized message
     */
    String get(Locale locale, String key, Object... args);

    /**
     * Gets the message identified by `key` for the first locale from the given set of locale that provide a value.
     * The message can be parameterized using  `args`, applied to the message using  {@link java.text.MessageFormat}.
     * If the message is not provided for the given locales, the default locale is tried. If the message is still not
     * provided, {@literal null} is returned.
     *
     * @param locales the ordered set of locales
     * @param key     the key
     * @param args    the arguments (optional)
     * @return the formatted internationalized message
     */
    String get(Locale[] locales, String key, Object... args);

    /**
     * Gets all the messages defined in the given locale AND default locale (for messages not defined in the given
     * locale). The returned map is composed pair of key:message.
     *
     * @param locale the locale
     * @return the set of defined messages.
     */
    Map<String, String> getAllMessages(Locale locale);

    /**
     * Gets all the messages defined in the given locales AND default locale (for messages not defined in the given
     * any locale). The message are added to the map only if they are not provided in the previous locale,
     * meaning that the order is important. The returned map is composed pair of key:message.
     *
     * @param locales the ordered set of locales
     * @return the set of defined messages.
     */
    Map<String, String> getAllMessages(Locale... locales);

    /**
     * Retrieves the ETAG for the given locale.
     * @param locale the locale
     * @return the computed etag, must not be {@code null} or empty
     */
    String etag(Locale locale);
}
