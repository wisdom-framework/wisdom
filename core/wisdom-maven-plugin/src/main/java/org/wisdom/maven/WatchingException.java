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
package org.wisdom.maven;

import java.io.File;

/**
 * Common exception thrown by Watchers.
 */
public class WatchingException extends Exception {

    private final File file;

    private final int line;
    private final int character;
    private final String title;

    /**
     * Creates a Watching Exception.
     *
     * @param message the error message
     */
    public WatchingException(String message) {
        this(message, null, null);
    }

    /**
     * Creates a Watching Exception.
     *
     * @param message the error message
     * @param cause   the cause of the error, if known.
     */
    public WatchingException(String message, Throwable cause) {
        this(message, null, cause);
    }

    /**
     * Creates a Watching Exception.
     *
     * @param message the error message
     * @param file    the file having thrown the exception while being processed.
     * @param cause   the cause of the error, if known.
     */
    public WatchingException(String message, File file, Throwable cause) {
        this("Watching Exception", message, file,
                -1, -1, cause);
    }

    public WatchingException(String title, String message, File file, Throwable cause) {
        this(title, message, file,
                -1, -1, cause);
    }

    public WatchingException(String title, String message, File file, int line, int character, Throwable cause) {
        super(message, cause);
        this.title = title;
        this.file = file;
        this.line = line;
        this.character = character;
    }

    /**
     * Gets the file having thrown the exception while being processed.
     *
     * @return the file, {@literal null} if unknown or not indicated.
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the line having triggered the issue. It's the line from the file returned by {@link #getFile()}.
     *
     * @return the line number, {@value -1} is there are no line specified.
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the character / row having triggered the issue. It's the character from the line returned by {@link
     * #getLine()}, from the {@link #getFile()} file.
     *
     * @return the character number, {@value -1} is there are no character specified.
     */
    public int getCharacter() {
        return character;
    }

    /**
     * Gets the title if any.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

}
