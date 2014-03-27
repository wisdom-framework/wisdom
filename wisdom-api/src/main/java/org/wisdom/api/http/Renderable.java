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
package org.wisdom.api.http;

import java.io.InputStream;

/**
 * Renderable is a placeholder for the content to be sent to the client. It allows customizing the rendering process.
 *
 * @param <T> the type of content.
 */
public interface Renderable<T> {

    /**
     * Retrieves the content. The content is read by teh underlying server to composed the messages sent to the client.
     *
     * @param context the HTTP context
     * @param result  the result having built this renderable object
     * @return the input stream to retrieve the content.
     * @throws RenderableException if the content cannot be rendered
     */
    InputStream render(Context context, Result result) throws RenderableException;

    /**
     * Gets the length of the rendered content. If the length is non known or cannot be determined at that time
     * {@literal -1} is returned.
     *
     * @return the rendered content length, or {@literal -1} is unknown.
     */
    long length();

    /**
     * Gets the mime-type of the rendered content. If the mime-type is unknown, {@literal null} is returned.
     *
     * @return the mime type or {@literal null} if unknown
     */
    String mimetype();

    /**
     * Retrieves the raw content.
     *
     * @return the content
     */
    T content();

    /**
     * Does the rendering process of this object requires external serializer, i.e. an external processing to build
     * the message sent to the client.  Such processing is implemented in a {@link org.wisdom.api.content
     * .ContentSerializer} matching the mime-type of the current renderable.
     *
     * @return {@literal true} if this renderable requires additional / external processing before being sent to the
     * client, {@literal false} otherwise.
     */
    boolean requireSerializer();

    /**
     * When {@link #requireSerializer()} returns {@literal true}, the {@link org.wisdom.api.content
     * .ContentSerializer} uses this method to set the final form of the renderable object. This 'form' is then sent
     * to the client.
     *
     * @param serialized the serialized form
     */
    void setSerializedForm(String serialized);

    /**
     * Checks whether the current renderable must be sent as chunk to the client. It's often the case for large
     * files, or content with unknown length.
     *
     * @return {@literal true} if the current renderable must be sent as chunk to the client.
     */
    boolean mustBeChunked();
}
