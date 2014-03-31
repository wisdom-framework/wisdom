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
 */
public class RenderableStream implements Renderable<InputStream> {

    private final InputStream stream;
    private final boolean mustBeChunked;

    public RenderableStream(InputStream stream, boolean mustBeChunked) {
        this.stream = stream;
        this.mustBeChunked = mustBeChunked;
    }

    public RenderableStream(InputStream stream) {
        this(stream, true);
    }

    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        return stream;
    }

    @Override
    public long length() {
        return -1; // Unknown.
    }

    @Override
    public String mimetype() {
        return null;
    }

    @Override
    public InputStream content() {
        return stream;
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
