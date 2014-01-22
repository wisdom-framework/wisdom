package org.wisdom.api.content;


public interface ContentEngine {

    BodyParser getBodyParserEngineForContentType(String contentType);
    ContentSerializer getContentSerializerForContentType(String contentType);
    ContentCodec getContentCodecForEncodingType(String type);
    ContentEncodingHelper getContentEncodingHelper();
}
