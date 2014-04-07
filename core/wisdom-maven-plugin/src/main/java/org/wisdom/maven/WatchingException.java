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
        super(message, cause);
        this.file = file;
    }

    /**
     * Gets the file having thrown the exception while being processed.
     *
     * @return the file, {@literal null} if unknown or not indicated.
     */
    public File getFile() {
        return file;
    }

}
