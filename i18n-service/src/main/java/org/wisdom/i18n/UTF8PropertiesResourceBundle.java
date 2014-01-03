package org.wisdom.i18n;

import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * UTF-8 {@link ResourceBundle} implementation. This implementation overrides
 * the {@link java.util.ResourceBundle#handleGetObject(java.lang.String)} method
 * on a wrapped {@link PropertyResourceBundle} to support UTF-8.
 */
public class UTF8PropertiesResourceBundle extends ResourceBundle {

    /**
     * The wrapped property resource bundle.
     */
    private PropertyResourceBundle bundle;

    /**
     * Creates a UTF8PropertiesResourceBundle
     *
     * @param is an input stream to create the {@link PropertyResourceBundle}.
     * @throws IOException if the input stream cannot be read, or if the
     *                     {@link PropertyResourceBundle} cannot be created correctly.
     */
    public UTF8PropertiesResourceBundle(InputStream is) throws IOException {
        bundle = new PropertyResourceBundle(is);
    }

    /**
     * Gets the keys contained in the wrapped {@link PropertyResourceBundle}.
     *
     * @return the list of key.
     * @see java.util.ResourceBundle#getKeys()
     */
    public Enumeration<String> getKeys() {
        return bundle.getKeys();
    }

    /**
     * This methods reads the value associated with the given key from the
     * wrapped {@link PropertyResourceBundle} using ISO-8859-1, and returned a
     * UTF-8 string.
     *
     * @param key the key
     * @return the UTF-8 encoded localized value.
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     */
    protected Object handleGetObject(String key) {
        String value = (String) bundle.handleGetObject(key);
        if (value == null) {
            return null;
        } else {
            // This strange hack let us read UTF-8 characters.
            return new String(value.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
        }
    }
}