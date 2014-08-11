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

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.wisdom.api.http.FileItem;

/**
 * Created by clement on 10/08/2014.
 */
public abstract class VertxFileUpload implements FileItem {

    protected final HttpServerFileUpload upload;

    protected VertxFileUpload(HttpServerFileUpload upload) {
        this.upload = upload;
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

    public abstract void cleanup();

    public void push(Buffer buffer) {
        throw new UnsupportedOperationException("Can't push data here");
    }
}
