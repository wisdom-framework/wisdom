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
 *
 */
public class FakeFileItem implements FileItem {

    private final File file;
    private final String field;

    public FakeFileItem(File file, String field) {
        this.file = file;
        this.field = field;
    }

    @Override
    public String field() {
        return field;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public byte[] bytes() {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream stream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(file);
    }

    @Override
    public long size() {
        return file.length();
    }
}
