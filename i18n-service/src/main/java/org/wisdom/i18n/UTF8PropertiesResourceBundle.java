package org.wisdom.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
     * @param bundle the wrapped property resource bundle.
     */
    public UTF8PropertiesResourceBundle(PropertyResourceBundle bundle) {
        this.bundle = bundle;
    }

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
        try {
            if (value == null) {
                return null;
            } else {
                return new String(value.getBytes("ISO-8859-1"), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            // Shouldn't fail - but should we still add logging message?
            return null;
        }
    }
}