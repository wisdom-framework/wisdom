package org.wisdom.maven.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A helper giving access to build constants written in META-INF/build.properties.
 */
public class BuildConstants {

    public static final String CONSTANTS_PATH = "META-INF/build.properties";

    private static final Properties properties = new Properties();

    static {
        load();
    }

    private BuildConstants() {
        // Avoid direct instantiation.
    }

    private static void load() {
        InputStream is = BuildConstants.class.getClassLoader().getResourceAsStream(CONSTANTS_PATH);
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load the 'constants' file");
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
