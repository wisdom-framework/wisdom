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
package org.wisdom.api.bodies;

import org.apache.commons.io.FileUtils;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;

import java.io.File;
import java.io.InputStream;

/**
 * Render a file.
 */
public class RenderableFile implements Renderable<File> {


    private final File file;
    private boolean mustBeChunked;

    public RenderableFile(File file) {
        this(file, true);
    }

    public RenderableFile(File file, boolean mustBechunked) {
        this.file = file;
        this.mustBeChunked = mustBechunked;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return FileUtils.openInputStream(file);
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(file);
    }

    @Override
    public File content() {
        return file;
    }

    @Override
    public boolean requireSerializer() {
        return false;
    }

    @Override
    public void setSerializedForm(String serialized) {
        // Nothing because serialization is not supported for this renderable class.
    }

    @Override
    public boolean mustBeChunked() {
        return mustBeChunked;
    }

}
