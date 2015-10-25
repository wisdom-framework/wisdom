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
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;

/**
 * A {@link org.wisdom.api.http.FileItem} implementation that need to be overridden by classes defining the 'storage'
 * policy. This parent class just handles the basic methods that can be directly delegated to the wrapped
 * {@link HttpServerFileUpload}.
 */
public abstract class VertxFileUpload implements FileItem {

    /**
     * The Vert.X file upload object.
     */
    protected final HttpServerFileUpload upload;
    private final Handler<Result> errorHandler;

    /**
     * An error if the file upload fails.
     */
    protected Throwable error;

    /**
     * Creates the {@link org.wisdom.framework.vertx.file.VertxFileUpload}.
     *
     * @param upload       the {@link HttpServerFileUpload} that is uploaded.
     * @param errorHandler the error handler.
     */
    protected VertxFileUpload(HttpServerFileUpload upload, Handler<Result> errorHandler) {
        this.upload = upload;
        this.errorHandler = errorHandler;
    }

    public void report(Throwable t) {
        error = t;
        errorHandler.handle(new Result(Status.PAYLOAD_TOO_LARGE).render("Uploaded file too large")
                .as(MimeTypes.TEXT));
    }

    /**
     * The field name from the form.
     *
     * @return the name of the input element from the form having uploaded the file. It can be {@literal null} if the
     * file was not uploaded from a form.
     */
    @Override
    public String field() {
        return upload.name();
    }

    /**
     * The name of the file.
     *
     * @return the file name
     */
    @Override
    public String name() {
        return upload.filename();
    }

    /**
     * Gets the file mime type as passed by the browser.
     *
     * @return the mime type of the file, {@literal null} is not set.
     */
    @Override
    public String mimetype() {
        return upload.contentType();
    }

    /**
     * Notifies the implementation that the upload is complete. The default implementaiton does nothing.
     */
    public void close() {
        // Nothing by default.
    }

    /**
     * Gets the size of the uploaded item.
     *
     * @return the size of the uploaded file.
     */
    @Override
    public long size() {
        return upload.size();
    }

    /**
     * @return the error if any.
     */
    public Throwable getErrorIfAny() {
        return error;
    }

    /**
     * Method called when the uploaded items are not used anymore. A cleanup policy may be provided,
     * for example removing the files created on the file system.
     */
    public void cleanup() {
        // Nothing by default.
    }

    /**
     * As the upload is done chunk by chunk, this method is called to give a new chunk. Implementation managing the
     * storage of the file must implement this method to retrieve the data.
     *
     * @param buffer the chunk
     */
    public void push(Buffer buffer) {
        throw new UnsupportedOperationException("Can't push data here");
    }
}
