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
            throw new IllegalStateException("Cannot load the 'constants' file", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Gets a build constant value.
     *
     * @param key the key
     * @return the value, {@code null} if not defined
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }
}
