package org.ow2.chameleon.wisdom.bodyparsers.engine;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.bodyparser.BodyParser;
import org.ow2.chameleon.wisdom.api.bodyparser.BodyParserEngine;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Body Parser Engine.
 */
@Component
@Provides
@Instantiate(name = "BodyParserEngine")
public class Engine implements BodyParserEngine {

    @Requires(specification = BodyParser.class)
    List<BodyParser> parsers;

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
}
