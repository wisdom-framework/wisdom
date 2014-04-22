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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.*;

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
    @Requires(specification = ContentCodec.class, optional = true)
    List<ContentCodec> encoders;
    @Requires(specification = ContentEncodingHelper.class, optional = true)
    ContentEncodingHelper encodingHelper;

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

    @Override
    public ContentCodec getContentCodecForEncodingType(String encoding) {
        for (ContentCodec codec : encoders) {
            if (codec.getEncodingType().equals(encoding)) {
                return codec;
            }
        }
        return null;
    }

    @Override
    public ContentEncodingHelper getContentEncodingHelper() {
        return encodingHelper;
    }
}
