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
package org.wisdom.framework.vertx.file;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerFileUpload;
import org.apache.commons.io.FileUtils;
import org.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of {@link org.wisdom.api.http.FileItem} storing the uploaded file in memory. This class is not
 * responsible of reading the file upload, but only to store the data. Notice that this implementation is not
 * suitable for large file.
 */
public class MemoryFileUpload extends VertxFileUpload {

    /**
     * The buffer storing the data.
     */
    Buffer buffer = Buffer.buffer(0);

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.file.MemoryFileUpload}.
     *
     * @param upload       the Vert.X file upload object
     * @param errorHandler the error handler
     */
    public MemoryFileUpload(HttpServerFileUpload upload, Handler<Result> errorHandler) {
        super(upload, errorHandler);

    }

    /**
     * Stores the new chunk in the buffer.
     *
     * @param buffer the chunk
     */
    public void push(Buffer buffer) {
        this.buffer.appendBuffer(buffer);
    }

    /**
     * Nothing to do, the buffer will be released.
     */
    public void cleanup() {
        this.buffer = Buffer.buffer(0);
    }

    /**
     * Gets the bytes.
     *
     * @return the full content of the file.
     */
    @Override
    public byte[] bytes() {
        return buffer.getBytes();
    }

    /**
     * Opens an input stream on the file.
     *
     * @return an input stream to read the content of the uploaded item.
     */
    @Override
    public InputStream stream() {
        return new ByteArrayInputStream(buffer.getBytes());
    }

    /**
     * Provides a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@literal true} if the file content is in memory.
     */
    @Override
    public boolean isInMemory() {
        return true;
    }

    /**
     * Gets a {@link java.io.File} object for this uploaded file. This file is a <strong>temporary</strong> file.
     * Depending on how is handled the file upload, the file may already exist, or not (in-memory) and then is created.
     *
     * @return a file object
     * @throws java.io.IOException if the file object cannot be created or retrieved
     * @since 0.7.1
     */
    @Override
    public File toFile() throws IOException {
        File temp = File.createTempFile("wisdom-fup", name());
        FileUtils.writeByteArrayToFile(temp, bytes());
        return temp;
    }
}
