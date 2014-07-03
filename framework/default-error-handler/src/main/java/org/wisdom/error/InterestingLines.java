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
package org.wisdom.error;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A structure storing the line of an error.
 */
public class InterestingLines {

    /**
     * The first line to display (line number).
     */
    public final int firstLine;

    /**
     * The line where the error occurs (line number).
     */
    public final int errorLine;

    /**
     * The set of lines.
     */
    public final String[] focus;

    /**
     * Creates the interested line instance.
     *
     * @param firstLine the first line
     * @param focus     the set of lines
     * @param errorLine the error line.
     */
    InterestingLines(int firstLine, String[] focus, int errorLine) {
        this.firstLine = firstLine;
        this.errorLine = errorLine;
        // We keep a copy of the array.
        this.focus = focus; //NOSONAR
    }

    /**
     * Extracts interesting lines to be displayed to the user.
     *
     * @param source the source
     * @param line   the line responsible of the error
     * @param border number of lines to use as a border
     * @param logger the logger to use to report errors
     * @return the interested line structure
     */
    public static InterestingLines extractInterestedLines(String source, int line, int border, Logger logger) {
        try {
            if (source == null) {
                return null;
            }
            String[] lines = source.split("\n");
            int firstLine = Math.max(0, line - border);
            int lastLine = Math.min(lines.length - 1, line + border);
            List<String> focusOn = new ArrayList<>();
            focusOn.addAll(Arrays.asList(lines).subList(firstLine, lastLine + 1));
            return new InterestingLines(firstLine + 1, focusOn.toArray(new String[focusOn.size()]), line - firstLine - 1);
        } catch (Exception e) {
            logger.error("Cannot extract the interesting lines", e);
            return null;
        }
    }
}
