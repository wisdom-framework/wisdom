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
import org.wisdom.api.http.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Render a file.
 */
public class RenderableFile implements Renderable<File> {


    private final File file;
    private boolean mustBeChunked;

    /**
     * Creates a new instance of {@link RenderableFile} serving the given file. The file is be sent chunk by chunk.
     *
     * @param file the file to serve
     */
    public RenderableFile(File file) {
        this(file, true);
    }

    /**
     * Creates a new instance of {@link RenderableFile} serving the given file.
     *
     * @param file  the file to serve
     * @param chunk whether or not the file should be sent chunk by chunk. In other world, whether or not the file
     *              need to be sent using the {@literal Chunked Transfert Encoding}.
     * @see <a href="http://en.wikipedia.org/wiki/Chunked_transfer_encoding">Chunked Transfert Encoding</a>
     */
    public RenderableFile(File file, boolean chunk) {
        this.file = file;
        this.mustBeChunked = chunk;
    }

    /**
     * Renders the file. If just returns an empty stream on the served file.
     *
     * @param context the HTTP context
     * @param result  the result having built this renderable object
     * @return the input stream. Be aware that the stream may be blocking.
     * @throws RenderableException if the file cannot be read.
     */
    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        try {
            return FileUtils.openInputStream(file);
        } catch (IOException e) {
            throw new RenderableException("Cannot read file " + file.getAbsolutePath(), e);
        }
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
