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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.streams.Pump;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Result;

import java.io.*;

/**
 * An implementation of {@link org.wisdom.api.http.FileItem} storing the uploaded file on disk. This class is not
 * responsible of reading the file upload, but only to store the data to a file on disk.
 */
public class DiskFileUpload extends VertxFileUpload {

    /**
     * Proposed default MINSIZE as 16 KB.
     */
    public static final long MINSIZE = 0x4000;

    /**
     * Should delete file on exit (in normal exit)
     */
    public static boolean deleteOnExitTemporaryFile = true;

    /**
     * System temp directory
     */
    public static String baseDirectory = null;

    /**
     * The prefix used to create files.
     */
    public static final String prefix = "FUp_";

    /**
     * The created file.
     */
    private final File file;

    /**
     * The async file used fro transferring the data to the disk. Once the transfer is done, this field is not used.
     */
    private AsyncFile async;

    /**
     * The Vert.X instance.
     */
    private final Vertx vertx;

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.file.DiskFileUpload}.
     *
     * @param vertx        the Vert.X instance
     * @param upload       the Vert.X file upload object
     * @param errorHandler the error handler
     */
    public DiskFileUpload(Vertx vertx, HttpServerFileUpload upload, Handler<Result> errorHandler) {
        super(upload, errorHandler);
        this.file = tempFile(upload);
        this.vertx = vertx;
    }


    /**
     * Deletes the created file.
     */
    public void cleanup() {
        FileUtils.deleteQuietly(file);
    }

    /**
     * A new chunk has arrived, save it on disk.
     *
     * @param buffer the chunk
     */
    @Override
    public void push(final Buffer buffer) {
        if (async == null) {
            upload.pause();
            vertx.fileSystem().open(file.getAbsolutePath(), new OpenOptions().setCreate(true),
                    new Handler<AsyncResult<AsyncFile>>() {
                        @Override
                        public void handle(AsyncResult<AsyncFile> event) {
                            async = event.result();
                            async.exceptionHandler(new Handler<Throwable>() {
                                @Override
                                public void handle(Throwable event) {
                                    LoggerFactory
                                            .getLogger(this.getClass().getName())
                                            .error("Cannot write into {}", file.getAbsolutePath(), event);
                                }
                            });
                            async.write(buffer);
                            Pump.pump(upload, async).start();
                            upload.resume();
                        }
                    });
        }
    }

    /**
     * Upload completed, close the async file.
     */
    @Override
    public void close() {
        vertx.runOnContext(event -> async.close());
    }

    /**
     * Creates a temporary file.
     *
     * @return a new Temp File from getDiskFilename(), default prefix, postfix and baseDirectory
     */
    static synchronized File tempFile(HttpServerFileUpload upload) {
        String newpostfix;
        String diskFilename = new File(upload.filename()).getName();
        newpostfix = '_' + diskFilename;
        File tmpFile;
        try {
            if (baseDirectory == null) {
                // create a temporary file
                tmpFile = File.createTempFile(prefix, newpostfix);
            } else {
                tmpFile = File.createTempFile(prefix, newpostfix, new File(
                        baseDirectory));
            }
            if (deleteOnExitTemporaryFile) {
                tmpFile.deleteOnExit();
            }
            return tmpFile;
        } catch (IOException e) {
            // Really bad, can't create the tmp file.
            throw new IllegalStateException(e);
        }

    }

    /**
     * Gets the bytes.
     *
     * @return the full content of the file.
     */
    @Override
    public byte[] bytes() {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Opens an input stream on the file.
     *
     * @return an input stream to read the content of the uploaded item.
     */
    @Override
    public InputStream stream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Provides a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@literal true} if the file content is in memory.
     */
    @Override
    public boolean isInMemory() {
        return false;
    }

    /**
     * Gets the size of the uploaded item.
     *
     * @return the size of the uploaded file.
     */
    @Override
    public long size() {
        return file.length();
    }

    /**
     * Gets a {@link java.io.File} object for this uploaded file. This file is a <strong>temporary</strong> file.
     * Depending on how is handled the file upload, the file may already exist, or not (in-memory) and then is created.
     *
     * @return a file object
     * @since 0.7.1
     */
    @Override
    public File toFile() {
        return file;
    }
}
