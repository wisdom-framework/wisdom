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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * A converter taking a "properties" file as input and generating a "hocon" file. Basically, it convert every entry
 * into the hocon format. The conversion is a 'best effort' conversion and may introduce incompatibilities. So a
 * human review is required.
 * <p>
 * First it changes entry by entry, and does not compute the object structure supported by hocon. This was made on
 * purpose to reduce the learning curve.
 * <p>
 * Translations rules are the following:
 * <ul>
 * <li>Keys are mapped to hocon key</li>
 * <li>Single line values are tranformed into quoted String value</li>
 * <li>If a value from the properties file is already quoted, the hocon value does not add quote</li>
 * <li>Multi-line values from the properties file are transformed to single line value (concatenation)</li>
 * <li>Unquoted, single-line values with "," are mapped to list</li>
 * <li>Comments are kept as comments</li>
 * </ul>
 */
public class Properties2HoconConverter {

    private static final Set<String> BOOLEAN_VALUES = ImmutableSet.of("true", "false", "on", "off", "yes", "no");

    private Properties2HoconConverter() {
        // Avoid direct instantiation.
    }

    /**
     * Converts the given properties file (props) to hocon.
     *
     * @param props  the properties file, must exist and be a valid properties file.
     * @param backup whether or not a backup should be generated. It backup is set to true, a ".backup" file is
     *               generated before the conversion.
     * @return the output file. It's the input file, except if the props' name does not end with ".conf", in that
     * case it generates a new file with this extension.
     * @throws IOException if something bad happens.
     */
    public static File convert(File props, boolean backup) throws IOException {
        if (!props.isFile()) {
            throw new IllegalArgumentException("The given properties file does not exist : " + props.getAbsolutePath());
        }

        Properties properties;
        try {
            properties = readFileAsProperties(props);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot convert " + props.getName() + " to hocon - the file is not a " +
                    "valid properties file", e);
        }

        // Properties cannot be null there, but might be empty
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert an empty file : " + props.getAbsolutePath());
        }

        // The conversion can start
        if (backup) {
            // Generate backup
            try {
                generateBackup(props);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot generate the backup for " + props.getName(), e);
            }
        }

        String hocon = convertToHocon(props);
        File output = props;
        if (!props.getName().endsWith(".conf")) {
            // Replace extension
            output = new File(props.getParentFile(),
                    props.getName().substring(0, props.getName().lastIndexOf(".")) + ".conf");
        }
        FileUtils.write(output, hocon);
        return output;
    }

    /**
     * Generates the hocon string resulting from the conversion of the given properties file.
     *
     * @param props the properties file, must exist and be a valid properties file.
     * @return the converted configuration (hocon format).
     * @throws IOException
     */
    public static String convertToHocon(File props) throws IOException {
        StringBuilder output = new StringBuilder();
        List<String> lines = FileUtils.readLines(props);


        boolean readingValue = false;
        for (String line : lines) {
            if (isComment(line)) {
                // Comment
                output.append("#").append(line.trim().substring(1)).append("\n");
                continue;
            }

            if (line.trim().isEmpty()) {
                if (!readingValue) {
                    output.append("\n");
                }
                continue;
            }

            if (!readingValue) {
                int position = getKeyValueSeparatorPosition(line);
                if (position != 0) {
                    String key = line.substring(0, position).trim();
                    int vPosition = getValueStartPosition(line, position);
                    String value;
                    if (vPosition == -1) {
                        // No value
                        value = "";
                    } else {
                        value = fixValue(line.substring(vPosition).trim());
                    }
                    output.append(fixKey(key)).append("=");
                    if (value.endsWith("\\")) {
                        // Multiline
                        readingValue = true;
                        output.append("\"");
                        output.append(value.substring(0, value.length() - 1));
                    } else {
                        if (vPosition == -1) {
                            output.append("\"\"");
                        } else {
                            output.append(fixSingleLineValue(line.substring(vPosition)).trim()).append("\n");
                        }
                        readingValue = false;
                    }
                } else {
                    throw new IllegalStateException("No key-value separator found in " + line);
                }
            } else {
                String value = line.trim();
                if (value.endsWith("\\")) {
                    // Multiline
                    readingValue = true;
                    output.append(fixValue(value.substring(0, value.length() - 1)));
                } else {
                    output.append(fixValue(value)).append("\"\n");
                    readingValue = false;
                }
            }
        }

        return output.toString();

    }

    private static boolean isComment(String line) {
        final String trim = line.trim();
        return trim.startsWith("#")
                || trim.startsWith("!");
    }

    private static String fixValue(String value) {
        if (value.isEmpty()) {
            return value;
        }

        return value
                .replace("\\#", "#")
                .replace("\\!", "!")
                .replace("\\=", "=")
                .replace("\\:", ":")
                .replace("\b", "b");
    }

    private static String fixSingleLineValue(String value) {
        if (value.isEmpty()) {
            return "";
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value;
        }

        String escaped = fixValue(value);
        if (escaped.contains(",")) {
            // The value is considered as a list
            return "[" + escaped + "]";
        } else if (isBoolean(escaped) || isNumeric(escaped)) {
            // The value is a number or a boolean
            return escaped;
        } else if (escaped.contains("${") && escaped.contains("}")) {
            // The value contains substitution
            return escaped;
        } else {
            return "\"" + escaped + "\"";
        }

    }

    private static boolean isNumeric(String escaped) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(escaped); //NOSONAR ignoring result.
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBoolean(String escaped) {
        String lc = escaped.toLowerCase();
        return BOOLEAN_VALUES.contains(lc);
    }

    private static String fixKey(String key) {
        if (key.indexOf(' ') != -1 || key.indexOf(':') != -1 || key.indexOf('=') != -1) {
            String unescape = key.trim()
                    .replace("\\=", "=")
                    .replace("\\:", ":")
                    .replace("\\ ", " ")
                    .replace("\b", "b");
            return "\"" + unescape + "\"";
        } else {
            return key.replace("\b", "b").trim();
        }
    }

    private static int getKeyValueSeparatorPosition(String line) {
        int position = 0;
        int positionOfEqual = getIndexOf(line, '=');
        if (positionOfEqual != -1) {
            position = positionOfEqual;
        }
        int positionOfColumn = getIndexOf(line, ':');
        if (isBefore(positionOfColumn, positionOfEqual)) {
            position = positionOfColumn;
        }
        int positionOfSpace = getIndexOf(line, ' ');
        if (isBefore(positionOfSpace, positionOfEqual) && isBefore(positionOfSpace, positionOfColumn)) {
            position = positionOfSpace;
        }

        if (position == 0) {
            position = line.length();
        }
        return position;
    }

    private static int getValueStartPosition(String line, int keyIndex) {
        int i = keyIndex;
        for (; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!Character.isSpaceChar(c) && c != '=' && c != ':') {
                return i;
            }
        }
        return -1;
    }

    private static boolean isBefore(int index1, int index2) {
        return index1 != -1 && (index2 == -1 || index1 < index2);
    }

    private static int getIndexOf(String line, char c) {
        int begin = 0;
        int i = 0;
        // Skip any space character.
        for (; begin == 0 && i < line.length(); i++) {
            if (!Character.isSpaceChar(line.charAt(i))) {
                begin = i + 1;
            }
        }
        if (i == line.length()) {
            return -1;
        }
        int pos = line.indexOf(c, begin);
        while (pos != -1) {
            if (pos == 0) {
                return 0;
            } else if (line.charAt(pos - 1) == '\\') {
                // Escaped
                pos = line.indexOf(c, pos + 1);
            } else {
                return pos;
            }
        }
        return -1;
    }


    private static void generateBackup(File props) throws IOException {
        File backup = new File(props.getParentFile(), props.getName() + ".backup");
        FileUtils.copyFile(props, backup);
    }

    private static Properties readFileAsProperties(File props) throws IOException {
        Properties properties = new Properties();
        InputStream stream = null;
        try {
            stream = FileUtils.openInputStream(props);
            properties.load(stream);
            return properties;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
