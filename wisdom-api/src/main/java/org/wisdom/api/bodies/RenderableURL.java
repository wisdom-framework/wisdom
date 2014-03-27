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

import org.wisdom.api.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A renderable object taking an URL as parameter.
 */
public class RenderableURL implements Renderable<URL> {

    private final URL url;
    private final boolean mustBeChunked;

    public RenderableURL(URL url, boolean mustBeChunked) {
        this.url = url;
        this.mustBeChunked = mustBeChunked;
    }

    public RenderableURL(URL url) {
        this(url, true);
    }

    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new RenderableException("Cannot open stream " + url.toExternalForm(), e);
        }
    }

    @Override
    public long length() {
        return -1; // Unknown.
    }

    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(url);
    }

    @Override
    public URL content() {
        return url;
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
