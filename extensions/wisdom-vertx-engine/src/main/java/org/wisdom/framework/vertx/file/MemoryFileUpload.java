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

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by clement on 10/08/2014.
 */
public class MemoryFileUpload extends VertxFileUpload {

    final Buffer buffer;

    public MemoryFileUpload(HttpServerFileUpload upload) {
        super(upload);
        buffer = new Buffer();
        upload.dataHandler(
                new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        if (event != null) {
                            buffer.appendBuffer(event);
                        }
                    }
                }
        );
    }

    public void cleanup() {
        // Nothing do do.
    }

    /**
     * Gets the byte.
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


}
