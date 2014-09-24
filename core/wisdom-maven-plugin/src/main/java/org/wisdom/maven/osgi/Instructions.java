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
package org.wisdom.maven.osgi;

import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Utility method to manage bundle packaging instructions.
 */
public final class Instructions {

    private Instructions() {
        // Avoid direct instantiation
    }

    /**
     * Utility method to merge instructions from the given file into the given set of instructions. The instructions
     * from the given file override the existing instructions (when both contain the same instruction)
     *
     * @param properties the current instructions
     * @param extra      the file
     * @return the new set of instructions
     * @throws IOException if the file cannot be read
     */
    public static Properties merge(Properties properties, File extra) throws IOException {
        Properties props = load(extra);
        return mergeAndOverrideExisting(properties, props);
    }

    /**
     * Utility method to merge instructions from {@code props2} into the {@code props1}. The instructions
     * from {@code props2} override the instructions  from {@code props1} (when both contain the same instruction)
     *
     * @param props1 the first set of instructions
     * @param props2 the second set of instructions
     * @return the new set of instructions containing the instructions from {@code props2} merged into {@code props1}.
     */
    public static Properties mergeAndOverrideExisting(Properties props1, Properties props2) {
        Properties properties = new Properties();
        properties.putAll(props1);
        properties.putAll(props2);
        return properties;
    }

    /**
     * Utility method to merge instructions from {@code props2} into the {@code props1}. The instructions
     * from {@code props2} do not override the instructions  from {@code props1} (when both contain the same
     * instruction), so instructions from {@code props1} stay unchanged and are contained in the file set of
     * instructions.
     * <p>
     * Notice that entries with empty values from {@code props2} are <strong>not</strong> merged.
     *
     * @param props1 the first set of instructions
     * @param props2 the second set of instructions
     * @return the new set of instructions containing the instructions from {@code props2} merged into {@code props1}.
     */
    public static Properties mergeAndSkipExisting(Properties props1, Properties props2) {
        Properties properties = new Properties();
        properties.putAll(props1);
        for (String key : props2.stringPropertyNames()) {
            if (!props1.containsKey(key) && !Strings.isNullOrEmpty(props2.getProperty(key))) {
                properties.put(key, props2.getProperty(key));
            }
        }
        return properties;
    }

    /**
     * Utility method to load a properties file.
     *
     * @param file the file
     * @return the read properties, empty if the file cannot be found.
     * @throws IOException if the file cannot be loaded.
     */
    public static Properties load(File file) throws IOException {
        if (file.isFile()) {
            FileInputStream fis = null;
            try {
                Properties headers = new Properties();
                fis = new FileInputStream(file);
                headers.load(fis);
                return headers;
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
        return new Properties();
    }

    /**
     * Utility method to sanitize the set of instructions.
     *
     * @param properties the current instructions
     * @return the new set of instructions
     */
    public static Properties sanitize(Properties properties) {
        // convert any non-String keys/values to Strings
        Properties sanitizedEntries = new Properties();
        for (Iterator<?> itr = properties.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry entry = (Map.Entry) itr.next();
            if (!(entry.getKey() instanceof String)) {
                String key = sanitize(entry.getKey());
                if (!properties.containsKey(key)) {
                    sanitizedEntries.setProperty(key, sanitize(entry.getValue()));
                }
                itr.remove();
            } else if (!(entry.getValue() instanceof String)) {
                entry.setValue(sanitize(entry.getValue()));
            }
        }
        properties.putAll(sanitizedEntries);
        return properties;
    }

    private static String sanitize(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Iterable) {
            String delim = "";
            StringBuilder buf = new StringBuilder();
            for (Object i : (Iterable<?>) value) {
                buf.append(delim).append(i);
                delim = ", ";
            }
            return buf.toString();
        } else if (value.getClass().isArray()) {
            String delim = "";
            StringBuilder buf = new StringBuilder();
            for (int i = 0, len = Array.getLength(value); i < len; i++) {
                buf.append(delim).append(Array.get(value, i));
                delim = ", ";
            }
            return buf.toString();
        } else {
            return String.valueOf(value);
        }
    }
}
