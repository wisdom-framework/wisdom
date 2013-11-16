package org.ow2.chameleon.wisdom.content.bodyparsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.content.BodyParser;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.content.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@Provides
@Instantiate
public class BodyParserJson implements BodyParser {

    private final Logger logger = LoggerFactory.getLogger(BodyParserJson.class);

    public <T> T invoke(Context context, Class<T> classOfT) {
        T t = null;
        try {
            t = Json.mapper().readValue(context.getReader(), classOfT);
        } catch (IOException e) {
            logger.error("Error parsing incoming Json", e);
        }

        return t;
    }

    @Override
    public <T> T invoke(byte[] bytes, Class<T> classOfT) {
        T t = null;
        try {
            t = Json.mapper().readValue(bytes, classOfT);
        } catch (IOException e) {
            logger.error("Error parsing incoming Json", e);
        }

        return t;
    }

    public String getContentType() {
        return MimeTypes.JSON;
    }

}
