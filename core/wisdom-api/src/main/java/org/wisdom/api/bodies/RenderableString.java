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
import org.wisdom.api.http.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * A renderable object holding a String content. However Strings can be used to store many different content such as
 * HTML, plain text... So the mime-type should be specified. Notice that if the mime-type of the content is not
 * specified, {@literal text/html} is used.
 */
public class RenderableString implements Renderable<String> {

    /**
     * The rendered content.
     */
    private String rendered;

    /**
     * The mime-type of the content.
     * Indeed Strings can be used to store many different content such as HTML, plain text...
     */
    private String type;

    /**
     * Whether or not this String needs to be serialized or not.
     */
    private boolean needSerializer = false;

    /**
     * Creates a new Renderable String. As the mime-type is not specified, {@literal text/html} is used.
     *
     * @param content the content, must not be {@literal null}
     */
    public RenderableString(String content) {
        this(content, null);
    }

    /**
     * Creates a new Renderable String. As the mime-type is not specified, {@literal text/html} is used.
     *
     * @param content the content, must not be {@literal null}
     */
    public RenderableString(StringBuilder content) {
        this(content.toString(), null);
    }

    /**
     * Creates a new Renderable String. As the mime-type is not specified, {@literal text/html} is used.
     *
     * @param content the content, must not be {@literal null}
     */
    public RenderableString(StringBuffer content) {  //NOSONAR
        this(content.toString(), null);
    }

    /**
     * Creates a new Renderable String. As the mime-type is not specified, {@literal text/html} is used.
     * This constructor calls {@link Object#toString()} on the given object.
     *
     * @param object the content, must not be {@literal null}
     */
    public RenderableString(Object object) {
        this(object.toString(), null);
    }

    /**
     * Creates a new Renderable String. This constructor calls {@link Object#toString()} on the given object.
     *
     * @param object the content, must not be {@literal null}
     * @param type   the mime type
     */
    public RenderableString(Object object, String type) {
        this(object.toString(), type);
    }

    /**
     * Creates a new Renderable String. As the mime-type is not specified, {@literal text/html} is used.
     *
     * @param content the content, must not be {@literal null}
     */
    public RenderableString(StringWriter content) {
        this(content.toString(), null);
    }

    /**
     * Creates a new Renderable String.
     *
     * @param content the content, must not be {@literal null}
     * @param type    the mime type
     */
    public RenderableString(String content, String type) {
        this.rendered = content;
        this.type = type;
    }

    /**
     * Retrieves the content.
     *
     * @param context the HTTP context
     * @param result  the result having built this renderable object
     * @return an input stream on the contained String.
     * @throws RenderableException should not happen
     */
    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        byte[] bytes;

        // We have a result, charset have to be provided
        if (result != null) {
            if (result.getCharset() == null) {
                // No charset provided, use default encoding (UTF-8).
                result.with(Charsets.UTF_8);
            }
            bytes = rendered.getBytes(result.getCharset());
        } else {
            //No Result, use the default encoding
            bytes = rendered.getBytes(Charsets.UTF_8);
        }

        return new ByteArrayInputStream(bytes);
    }

    /**
     * Gets the length of the contained String.
     *
     * @return the length of the contained String.
     */
    @Override
    public long length() {
        return rendered.length();
    }

    /**
     * @return the mime-type.
     */
    @Override
    public String mimetype() {
        if (type == null) {
            return MimeTypes.HTML;
        } else {
            return type;
        }
    }

    /**
     * Sets the mime-type of the content stored in this renderable. If 'type' is JSON or XML,
     * it checks whether the content is already JSON or XML encoded. If not, it enables the flag requiring the
     * serialization. The detection is pretty basic and is based on the presence of "{}" or "[]" for JSON and "&lt;&gt;"
     * for XML at the beginning and at the end of the sequence.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
        if (type.equals(MimeTypes.JSON)) {
            // Checks whether or not the given String is already a JSON string.
            // We apply a very simple check ({} or []).
            needSerializer = !((rendered.startsWith("{") && rendered.endsWith("}"))
                    || (rendered.startsWith("[") && rendered.endsWith("]")));
        } else if (type.equals(MimeTypes.XML)) {
            // Checks whether or not the given String is already a XML string.
            // We apply a very simple check: <>
            needSerializer = !(rendered.startsWith("<") && rendered.endsWith(">"));
        }
    }

    /**
     * @return the content.
     */
    @Override
    public String content() {
        return rendered;
    }

    /**
     * @return {@literal false}, as no external processing is required.
     */
    @Override
    public boolean requireSerializer() {
        return needSerializer;
    }

    /**
     * Not used.
     *
     * @param serialized the serialized form
     */
    @Override
    public void setSerializedForm(String serialized) {
        rendered = serialized;
    }

    /**
     * this renderable implementation must not be use for large Strings. So, we don't send the content as chunk.
     *
     * @return {@literal false}
     */
    @Override
    public boolean mustBeChunked() {
        return false;
    }

}
