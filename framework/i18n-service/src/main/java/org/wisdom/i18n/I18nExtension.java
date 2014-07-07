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
package org.wisdom.i18n;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * i18n structure.
 * It retrieves and provides messages from a {@link java.util.ResourceBundle} object.
 * Each extension handles one locale.
 */
public class I18nExtension {

    /**
     * The resource bundle object.
     */
    private ResourceBundle bundle;
    /**
     * The URL pointing to the resource.
     */
    private final URL resource;

    /**
     * The locale.
     */
    private final Locale locale;

    /**
     * The source bundle (can be null).
     */
    private final Bundle source;

    /**
     * Creates an extension.
     *
     * @param resource the resource
     * @param locale   the locale
     */
    public I18nExtension(URL resource,
                         Locale locale, Bundle source) {
        this.resource = resource;
        this.locale = locale;
        this.source = source;
    }

    /**
     * Checks whether the current object is equal to the given object. The check is based on the locale comparison.
     *
     * @param o the object
     * @return {@code true} if the given object is a {@link org.wisdom.i18n.I18nExtension} if if both have the same
     * locale.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        I18nExtension extension = (I18nExtension) o;

        return locale.equals(extension.locale)
                && resource.toExternalForm().equals(extension.resource.toExternalForm());

    }

    /**
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        int result = resource.toExternalForm().hashCode();
        result = 31 * result + locale.hashCode();
        return result;
    }

    /**
     * Loads the content of the I18N Extension by reading the content of {@link #resource}.
     *
     * @throws IOException if the content cannot be loaded
     */
    public void load() throws IOException {
        if (resource.toExternalForm().endsWith(".xml")) {
            // XML Format
            bundle = new XMLResourceBundle(resource.openStream());
        } else {
            // Properties file (loaded using UTF-8)
            bundle = new UTF8PropertiesResourceBundle(resource.openStream());
        }

        if (bundle.keySet().isEmpty()) {
            throw new IOException(resource.toExternalForm() + " is not a valid resource - no keys");
        }
    }

    /**
     * Gets the localized message for the given key.
     *
     * @param key  the key
     * @param args the message parameters, can be empty.
     * @return the localized message or {@literal null} if the key does not
     * exist in the bundle. If {@literal args} is not empty, the retrieve message is formatted using the given
     * arguments. This formatting uses the {@link java.text.MessageFormat#format(String, Object...)} method.
     */
    public String get(String key, Object... args) {
        String value = bundle.getString(key);
        if (args.length != 0) {
            value = MessageFormat.format(value, args);
        }
        return value;
    }

    /**
     * Gets the keys contained in the bundle.
     *
     * @return the keys. At least one key is returned as empty bundle
     * was rejected.
     */
    public Set<String> keys() {
        Set<String> list = new LinkedHashSet<>();
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            list.add(keys.nextElement());
        }
        return list;
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale locale() {
        return locale;
    }

    /**
     * @return the resource bundle object.
     */
    public ResourceBundle bundle() {
        return bundle;
    }

    /**
     * @return the bundle containing the given I18N Extension.
     */
    public Bundle source() {
        return source;
    }

    /**
     * @return the key - message map.
     */
    public Map<String, String> map() {
        Map<String, String> messages = new HashMap<>();
        for (String k : bundle.keySet()) {
            messages.put(k, get(k));
        }
        return messages;
    }
}
