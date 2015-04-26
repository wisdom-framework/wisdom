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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Empty body.
 */
public class NoHttpBody implements Renderable<Void> {

    /**
     * The singleton instance.
     */
    public static final NoHttpBody INSTANCE = new NoHttpBody();

    private NoHttpBody() {
        // Avoid direct instantiation.
    }

    /**
     * The body.
     */
    private static final byte[] EMPTY = new byte[0];

    /**
     * An accessor to access to the empty array singleton.
     * @return the empty array.
     */
    public static byte[] empty() {
        return EMPTY;
    }

    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        return new ByteArrayInputStream(EMPTY);
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public String mimetype() {
        return MimeTypes.TEXT;
    }

    @Override
    public Void content() {
        return null;
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
        return false;
    }

}
