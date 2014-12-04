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
package org.wisdom.engine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A static class to access the constant written during packaging.
 * The properties re stored in the {@link #CONSTANTS_PATH} file.
 */
public final class BuildConstants {

    public static String CONSTANTS_PATH = "META-INF/constants.properties";
    public static String WISDOM_VERSION_KEY = "wisdom.version";
    public static String NETTY_VERSION_KEY = "netty.version";

    private static Properties m_properties;

    /**
     * Retrieves the current wisdom framework version.
     */
    public static final String WISDOM_VERSION;

    /**
     * Retrieves the Netty version used by the current engine.
     */
    public static final String NETTY_VERSION;

    static {
        load();
        WISDOM_VERSION = get(WISDOM_VERSION_KEY);
        NETTY_VERSION = get(NETTY_VERSION_KEY);
    }

    private static void load() {
        m_properties = new Properties();
        InputStream is = BuildConstants.class.getClassLoader().getResourceAsStream(CONSTANTS_PATH);
        try {
            m_properties.load(is);
            is.close();
        } catch (IOException e) { //NOSONAR
            throw new IllegalStateException("Cannot load the 'constants' file");
        }
    }

    /**
     * Gets the property value.
     *
     * @param key the property's key
     * @return the value, {@literal null} if not present in the loaded file.
     */
    public static String get(String key) {
        return m_properties.getProperty(key);
    }
}
