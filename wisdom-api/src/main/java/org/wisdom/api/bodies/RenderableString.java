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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;

/**
 * A renderable object taking a String as parameter.
 */
public class RenderableString implements Renderable<String> {

    //TODO Support encoding

    private final String rendered;
    private final String type;

    public RenderableString(String object) {
        this(object, null);
    }

    public RenderableString(StringBuilder object) {
        this(object.toString(), null);
    }

    public RenderableString(StringBuffer object) {
        this(object.toString(), null);
    }

    public RenderableString(Object object) {
        this(object.toString(), null);
    }

    public RenderableString(Object object, String type) {
        this(object.toString(), type);
    }

    public RenderableString(StringWriter object) {
        this(object.toString(), null);
    }

    public RenderableString(String object, String type) {
        rendered = object;
        this.type = type;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
    	byte[] bytes = null;

    	if(result != null){ // We have a result, charset have to be provided
    		if(result.getCharset() == null){ // No charset provided
    			result.with(Charset.defaultCharset()); // Set the default encoding
    		}
    		bytes = rendered.getBytes(result.getCharset());
    	}else{
    		//No Result, use the default platform encoding
    		bytes = rendered.getBytes();
    	}

        return new ByteArrayInputStream(bytes);
    }

    @Override
    public long length() {
        return rendered.length();
    }

    @Override
    public String mimetype() {
        if (type == null) {
            return MimeTypes.HTML;
        } else {
            return type;
        }
    }

    @Override
    public String content() {
        return rendered;
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
