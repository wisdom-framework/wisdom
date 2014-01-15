package org.wisdom.content.engines;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.content.BodyParser;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.content.ContentSerializer;
import org.slf4j.LoggerFactory;

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

    @Override
    public BodyParser getBodyParserEngineForContentType(String contentType) {
        for (BodyParser parser : parsers) {
            if (parser.getContentType().equals(contentType)) {
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
    public ContentCodec getContentEncoderForEncodingType(String encoding) {
        for (ContentCodec encoder : encoders) {
            if (encoder.getEncodingType().equals(encoding)) {
                return encoder;
            }
        }
    	return null;
    }
}
