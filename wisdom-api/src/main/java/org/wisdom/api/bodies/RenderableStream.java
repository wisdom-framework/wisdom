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

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.RenderableException;
import org.wisdom.api.http.Result;

import java.io.InputStream;

/**
 * A renderable object taking an Input Stream as parameter.
 * As the length of stream cannot be anticipated or computed, serving such renderable is done using HTTP Chunks.
 */
public class RenderableStream implements Renderable<InputStream> {

    private final InputStream stream;
    private final boolean mustBeChunked;

    /**
     * Creates a new renderable stream.
     *
     * @param stream        the stream
     * @param mustBeChunked enable or disable the chunk encoding. If set to {@literal false},
     *                      the stream will be copied in a byte array before being sent to the client. It may lead to
     *                      {@link java.lang.OutOfMemoryError} if the read data is huge.
     */
    public RenderableStream(InputStream stream, boolean mustBeChunked) {
        this.stream = stream;
        this.mustBeChunked = mustBeChunked;
    }

    /**
     * Creates a Renderable Stream from the given stream.
     *
     * @param stream the stream, must not be {@literal null}
     */
    public RenderableStream(InputStream stream) {
        this(stream, true);
    }

    /**
     * Renders the wrapped stream.
     *
     * @param context the HTTP context
     * @param result  the result having built this renderable object
     * @return the wrapped input stream directly. Whether or not this stream will be sent as chunk depends {@link
     * #mustBeChunked} field.
     * @throws RenderableException if something bad happened
     */
    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        return stream;
    }

    /**
     * @return as the length of a stream is unknown, returns {@literal -1}.
     */
    @Override
    public long length() {
        return -1; // Unknown.
    }

    /**
     * @return as the mime type of the stream is unknown, returns {@literal null}. Users should set the {@literal
     * Content-Type} header.
     */
    @Override
    public String mimetype() {
        return null;
    }

    /**
     * Gets the wrapped stream.
     *
     * @return the wrapped stream
     */
    @Override
    public InputStream content() {
        return stream;
    }

    /**
     * Streams do not need serializer as we assume the data is already serialized.
     *
     * @return {@literal false}
     */
    @Override
    public boolean requireSerializer() {
        return false;
    }

    /**
     * As streams do not need serializer, this method should not be used.
     *
     * @param serialized the serialized form
     */
    @Override
    public void setSerializedForm(String serialized) {
        // Nothing because serialization is not supported for this renderable class.
    }

    /**
     * @return whether or not the stream must be sent using HTTP chunks or direct. If {@literal false},
     * the whole stream is read and stored into a byte array and may lead to {@link java.lang.OutOfMemoryError}.
     */
    @Override
    public boolean mustBeChunked() {
        return mustBeChunked;
    }

}
