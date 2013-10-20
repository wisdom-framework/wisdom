package org.ow2.chameleon.wisdom.bodyparsers.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.bodyparser.BodyParser;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@Provides
@Instantiate
public class BodyParserJson implements BodyParser {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(BodyParserJson.class);

    public <T> T invoke(Context context, Class<T> classOfT) {
        T t = null;
        try {
            t = objectMapper.readValue(context.getReader(), classOfT);
        } catch (IOException e) {
            logger.error("Error parsing incoming Json", e);
        }

        return t;
    }

    public String getContentType() {
        return MimeTypes.JSON;
    }

}
