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
package org.wisdom.api.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an uploaded file.
 * This class represents a file or form item that was received within a <code>multipart/form-data</code> <em>POST</em>
 * request.
 */
public interface FileItem {

    /**
     * The field name from the form.
     *
     * @return the name of the input element from the form having uploaded the file. It can be {@literal null} if the
     *         file was not uploaded from a form.
     */
    String field();

    /**
     * The name of the file.
     *
     * @return the file name
     */
    String name();

    /**
     * Gets the bytes.
     *
     * @return the full content of the file.
     */
    byte[] bytes();

    /**
     * Opens an input stream on the file.
     *
     * @return an input stream to read the content of the uploaded item.
     */
    InputStream stream();

    /**
     * Provides a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@literal true} if the file content is in memory.
     */
    boolean isInMemory();

    /**
     * Gets the file mime type as passed by the browser.
     *
     * @return the mime type of the file, {@literal null} is not set.
     */
    String mimetype();

    /**
     * Gets the size of the uploaded item.
     *
     * @return the size of the uploaded file.
     */
    long size();

    /**
     * Gets a {@link java.io.File} object for this uploaded file. This file is a <strong>temporary</strong> file.
     * Depending on how is handled the file upload, the file may already exist, or not (in-memory) and then is created.
     * @return a file object
     * @throws java.io.IOException if the file object cannot be created or retrieved
     * @since 0.7.1
     */
    File toFile() throws IOException;
}
