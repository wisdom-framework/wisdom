package org.ow2.chameleon.wisdom.api.bodyparser;


public interface BodyParserEngine {

    BodyParser getBodyParserEngineForContentType(String contentType);

}
