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
package org.wisdom.api.content;


import com.google.common.net.MediaType;

import java.util.Collection;

/**
 * This interface is exposed by the content engine implementation as a service.
 * It allows retrieving the parsers (text to object) and serializers (object to text) to handle a specific mime-type.
 * <p/>
 * Implementations aggregates the available {@link org.wisdom.api.content.BodyParser} and
 * {@link org.wisdom.api.content.ContentSerializer} and chooses the 'right' one.
 */
public interface ContentEngine {

    /**
     * Gets a {@link org.wisdom.api.content.BodyParser} handling the given content type.
     *
     * @param contentType the content type
     * @return the parser, {@literal null} if none
     */
    BodyParser getBodyParserEngineForContentType(String contentType);

    /**
     * Gets a {@link org.wisdom.api.content.ContentSerializer} handling the given content type.
     *
     * @param contentType the content type
     * @return the serializer, {@literal null} if none
     */
    ContentSerializer getContentSerializerForContentType(String contentType);

    /**
     * Finds the 'best' content serializer for the given accept headers.
     *
     * @param mediaTypes the ordered set of {@link com.google.common.net.MediaType} from the {@code ACCEPT} header.
     * @return the best serializer from the list matching the {@code ACCEPT} header, {@code null} if none match
     */
    public ContentSerializer getBestSerializer(Collection<MediaType> mediaTypes);
}
