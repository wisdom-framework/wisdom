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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        I18nExtension extension = (I18nExtension) o;

        return locale.equals(extension.locale)
                && resource.toExternalForm().equals(extension.resource.toExternalForm());

    }

    @Override
    public int hashCode() {
        int result = resource.toExternalForm().hashCode();
        result = 31 * result + locale.hashCode();
        return result;
    }

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
     * @param key the key
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
     * @return the locale
     */
    public Locale locale() {
        return locale;
    }

    public ResourceBundle bundle() {
        return bundle;
    }

    public Bundle source() {
        return source;
    }

    public Map<String, String> map() {
        Map<String, String> messages = new HashMap<>();
        for (String k : bundle.keySet()) {
            messages.put(k, get(k));
        }
        return messages;
    }
}
