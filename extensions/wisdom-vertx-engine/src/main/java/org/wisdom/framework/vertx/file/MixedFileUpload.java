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
 * A smart implementation of {@link org.wisdom.api.http.FileItem} adapting the uploaded file storage according to the
 * upload size. As this size cannot be determined beforehand, it first tries to use a {@link org.wisdom.framework
 * .vertx.file.MemoryFileUpload}, and if a threshold is reached, it changes to {@link org.wisdom.framework.vertx.file
 * .DiskFileUpload}.
 */
public class MixedFileUpload extends VertxFileUpload {

    /**
     * The current instance of {@link org.wisdom.api.http.FileItem}. The value change when a amount of data uploaded
     * reached a threshold.
     */
    VertxFileUpload delegate;


    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.file.VertxFileUpload}.
     *
     * @param vertx     the Vert.X instance
     * @param upload    the upload object
     * @param limitSize the threshold. If the amount of uploaded data is below this limit,
     *                  {@link org.wisdom.framework.vertx.file.MemoryFileUpload} is used to backend the uploaded file.
     *                  Otherwise, it uses a {@link org.wisdom.framework.vertx.file.DiskFileUpload}.
     */
    public MixedFileUpload(final Vertx vertx, final HttpServerFileUpload upload, final long limitSize,
                           final long maxSize) {
        super(upload);
        delegate = new MemoryFileUpload(upload);
        upload.endHandler(new Handler<Void>() {
            /**
             * The upload is completed. Invokes the {@link VertxFileUpload#close()} method on the delegate object.
             * @param event irrelevant
             */
            @Override
            public void handle(Void event) {
                delegate.close();
            }
        })
                .dataHandler(

                new Handler<Buffer>() {
                    /**
                     * Handles a chunk of uploaded data. This method is responsible of selecting the right backend,
                     * and switches when the amount of data reached the given threshold.
                     *
                     * This method also checks that the uploaded file does not exceed the maximum allowed (file
                     * upload) size.
                     */
                    @Override
                    public void handle(Buffer event) {
                        if (event != null) {
                            if (delegate instanceof MemoryFileUpload) {
                                MemoryFileUpload mem = (MemoryFileUpload) delegate;
                                checkSize(mem.buffer.length() + event.length(), maxSize);
                                if (mem.buffer.length() + event.length() > limitSize) {
                                    // Switch to disk file upload.
                                    DiskFileUpload disk = new DiskFileUpload(vertx, upload);
                                    disk.push(mem.buffer);
                                    mem.cleanup();
                                    // No cleanup required for the memory based backend.
                                    delegate = disk;
                                }
                            }
                            delegate.push(event);
                        }
                    }
                }
        );
    }

    /**
     * Checks whether we exceed the max allowed file size.
     *
     * @param newSize the expected size once the current chunk is consumed
     * @param maxSize the max allowed size.
     * @throws IllegalStateException
     */
    private void checkSize(long newSize, long maxSize) throws IllegalStateException {
        if (maxSize >= 0 && newSize > maxSize) {
            throw new IllegalStateException("Size exceed allowed maximum capacity");
        }
    }

    /**
     * Delegated to the wrapped backend.
     */
    @Override
    public void cleanup() {
        delegate.cleanup();
    }

    /**
     * Gets the bytes.
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
