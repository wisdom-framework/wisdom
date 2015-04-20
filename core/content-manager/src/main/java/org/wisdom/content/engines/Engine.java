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
package org.wisdom.content.engines;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.*;

import java.util.Collection;
import java.util.List;

/**
 * Content Engine.
 */
@Component
@Provides
@Instantiate(name = "ContentEngine")
public class Engine implements ContentEngine {

    @Requires(specification = BodyParser.class, optional = true)
    List<BodyParser> parsers;
    @Requires(specification = ContentSerializer.class, optional = true)
    List<ContentSerializer> serializers;

    /**
     * Gets the body parser that can be used to parse a body with the given content type.
     *
     * @param contentType the content type
     * @return a body parser, {@code null} if none match
     */
    @Override
    public BodyParser getBodyParserEngineForContentType(String contentType) {
        for (BodyParser parser : parsers) {
            if (parser.getContentTypes().contains(contentType)) {
                return parser;
            }
        }
        LoggerFactory.getLogger(this.getClass()).info("Cannot find a body parser for " + contentType);
        return null;
    }

    /**
     * Gets the content serializer that can be used to serialize a result to the given content type. This method uses
     * an exact match.
     *
     * @param contentType the content type
     * @return a content serializer, {@code null} if none match
     */
    @Override
    public ContentSerializer getContentSerializerForContentType(String contentType) {
        for (ContentSerializer renderer : serializers) {
            if (renderer.getContentType().equals(contentType)) {
                return renderer;
            }
        }
        LoggerFactory.getLogger(this.getClass()).info("Cannot find a content renderer handling " + contentType);
        return null;
    }

    /**
     * Finds the 'best' content serializer for the given accept headers.
     *
     * @param mediaTypes the ordered set of {@link com.google.common.net.MediaType} from the {@code ACCEPT} header.
     * @return the best serializer from the list matching the {@code ACCEPT} header, {@code null} if none match
     */
    @Override
    public ContentSerializer getBestSerializer(Collection<MediaType> mediaTypes) {
        if (mediaTypes == null  || mediaTypes.isEmpty()) {
            mediaTypes = ImmutableList.of(MediaType.HTML_UTF_8);
        }
        for (MediaType type : mediaTypes) {
            for (ContentSerializer ser : serializers) {
                MediaType mt = MediaType.parse(ser.getContentType());
                if (mt.is(type.withoutParameters())) {
                    return ser;
                }
            }
        }
        return null;
    }
}
