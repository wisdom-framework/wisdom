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

import com.google.common.base.Charsets;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.RenderableException;
import org.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Render any object, but it requires serialization.
 */
public class RenderableObject implements Renderable<Object> {

    private final Object object;
    private String serialized;

    public RenderableObject(Object o) {
        this.object = o;
    }

    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        if (serialized == null) {
            throw new RenderableException("Serialization required before rendering");
        }
        return new ByteArrayInputStream(serialized.getBytes(Charsets.UTF_8));
    }

    @Override
    public void setSerializedForm(String serialized) {
        this.serialized = serialized;
    }

    @Override
    public boolean mustBeChunked() {
        return false;
    }

    @Override
    public long length() {
        return -1; // Unknown
    }

    @Override
    public String mimetype() {
        // Unknown !
        return null;
    }

    @Override
    public Object content() {
        return object;
    }

    @Override
    public boolean requireSerializer() {
        return true;
    }
}
