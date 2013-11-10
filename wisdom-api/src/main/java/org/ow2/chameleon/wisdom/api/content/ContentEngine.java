package org.ow2.chameleon.wisdom.api.content;


public interface ContentEngine {

    BodyParser getBodyParserEngineForContentType(String contentType);
    ContentSerializer getContentSerializerForContentType(String contentType);

}
