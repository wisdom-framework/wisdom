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
package org.wisdom.test.parents;

import org.apache.commons.io.FileUtils;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.MimeTypes;

import java.io.*;

/**
 * A fake implementation of File Item used in tests.
 */
public class FakeFileItem implements FileItem {

    /**
     * The file.
     */
    private final File file;

    /**
     * The field of the form having sent the file.
     */
    private final String field;

    /**
     * Creates a new fake file item.
     *
     * @param file  the file, must not be {@literal null}
     * @param field the field name, can be {@literal null}
     */
    public FakeFileItem(File file, String field) {
        this.file = file;
        this.field = field;
    }

    /**
     * @return the field, may be {@literal null}.
     */
    @Override
    public String field() {
        return field;
    }

    /**
     * @return the name of the file.
     */
    @Override
    public String name() {
        return file.getName();
    }

    /**
     * @return the content of the file.
     */
    @Override
    public byte[] bytes() {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return a stream on the content of the file.
     */
    @Override
    public InputStream stream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * This method is not usable from tests.
     *
     * @return {@literal false}
     */
    @Override
    public boolean isInMemory() {
        return false;
    }

    /**
     * Tries to guess the mime-type of the file by analyzing its extension.
     *
     * @return the mime-type, {@literal null} if unknown
     */
    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(file);
    }

    /**
     * @return the file's length.
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
