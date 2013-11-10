package org.ow2.chameleon.wisdom.content.serializers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.content.ContentSerializer;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.content.json.Json;

/**
 * Renders HTML content
 */
@Component
@Instantiate
@Provides
public class JSONSerializer implements ContentSerializer {

    @Override
    public String getContentType() {
        return MimeTypes.JSON;
    }

    @Override
    public void serialize(Renderable<?> renderable) {
        JsonNode node = Json.toJson(renderable.content());
        renderable.setSerializedForm(node.toString());
    }
}
