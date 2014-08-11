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
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;

import java.io.InputStream;

/**
 * Created by clement on 11/08/2014.
 */
public class MixedFileUpload extends VertxFileUpload {

    /**
     * Proposed default MAXSIZE = -1 as UNLIMITED
     */
    public static final long MAXSIZE = -1;

    VertxFileUpload delegate;


    public MixedFileUpload(final Vertx vertx, final HttpServerFileUpload upload, final long limitSize) {
        super(upload);
        delegate = new MemoryFileUpload(upload);
        upload.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                delegate.close();
            }
        }).dataHandler(
                new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        if (event != null) {
                            if (delegate instanceof MemoryFileUpload) {
                                MemoryFileUpload mem = (MemoryFileUpload) delegate;
                                checkSize(mem.buffer.length() + event.length());
                                if (mem.buffer.length() + event.length() > limitSize) {
                                    // Switch to disk file upload.
                                    DiskFileUpload disk = new DiskFileUpload(vertx, upload);
                                    disk.push(mem.buffer);
                                    delegate = disk;
                                }
                            }
                            delegate.push(event);
                        }
                    }
                }
        );
    }

    public void checkSize(long newSize) throws IllegalStateException {
        //TODO allow the configuration of min and max size.
        if (MAXSIZE >= 0 && newSize > MAXSIZE) {
            throw new IllegalStateException("Size exceed allowed maximum capacity");
        }
    }

    @Override
    public void cleanup() {
        delegate.cleanup();
    }

    /**
     * Gets the byte.
     *
     * @return the full content of the file.
     */
    @Override
    public byte[] bytes() {
        return delegate.bytes();
    }

    /**
     * Opens an input stream on the file.
     *
     * @return an input stream to read the content of the uploaded item.
     */
    @Override
    public InputStream stream() {
        return delegate.stream();
    }

    /**
     * Provides a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@literal true} if the file content is in memory.
     */
    @Override
    public boolean isInMemory() {
        return delegate.isInMemory();
    }
}
