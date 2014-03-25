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
package org.wisdom.engine.wrapper;

import io.netty.handler.codec.http.multipart.FileUpload;
import org.wisdom.api.http.FileItem;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File Item implementation for Netty.
 */
public class FileItemFromNetty implements FileItem {
    private final FileUpload upload;

    public FileItemFromNetty(FileUpload fileUpload) {
        this.upload = fileUpload;
    }

    public FileUpload upload() {
        return upload;
    }

    @Override
    public String field() {
        return upload.getName();
    }

    @Override
    public String name() {
        return upload.getFilename();
    }

    @Override
    public byte[] bytes() {
        try {
            return upload.get();
        } catch (IOException e) { //NOSONAR
            return null;
        }
    }

    @Override
    public InputStream stream() {
        if (upload.isInMemory()) {
            // If the data is in memory, just return an input stream on the byte array.
            return new ByteArrayInputStream(bytes());
        } else {
            // It's a file, probably huge, we can't get the bytes.
            try {
                return new FileInputStream(upload.getFile());
            } catch (IOException e) {
                throw new RuntimeException("Cannot retrieve the content of the uploaded file", e);
            }
        }
    }

    @Override
    public boolean isInMemory() {
        return upload.isInMemory();
    }

    @Override
    public String mimetype() {
        return upload.getContentType();
    }

    @Override
    public long size() {
        return upload.length();
    }
}
