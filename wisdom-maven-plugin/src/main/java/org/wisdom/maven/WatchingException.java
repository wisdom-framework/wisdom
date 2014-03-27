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
 * Common exception thrown by Watcher.
 */
public class WatchingException extends Exception {

    private File file;

    public WatchingException(String message) {
        super(message);
    }

    public WatchingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WatchingException(String message, File file, Throwable cause) {
        super(message, cause);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
