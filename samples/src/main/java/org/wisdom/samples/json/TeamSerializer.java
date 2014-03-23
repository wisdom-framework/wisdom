package org.wisdom.samples.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.*;
import org.wisdom.api.content.JacksonModuleRepository;

import java.io.IOException;

/**
 * A example of serializer.
 * By default it should return soemthing like:
 * <code>
 *     {"contributors":[{"firstName":"clement","lastName":"escoffier"},{"firstName":"nicolas","lastName":"rempulski"}]}
 * </code>
 * <p/>
 * This serializer transforms the output to:
 * <code>
 *     {
 *     "clement" : {"firstName":"clement","lastName":"escoffier"},
 *     "nicolas" : {"firstName":"nicolas","lastName":"rempulski"}
 *     }
 * </code>
 */
@Component(immediate = true)
@Instantiate
public class TeamSerializer {

    private final SimpleModule module;

    @Requires
    JacksonModuleRepository repository;

    public TeamSerializer() {
        module = new SimpleModule("My Team Module");
        module.addSerializer(Team.class, new JsonSerializer<Team>() {
            @Override
            public void serialize(Team team, JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider)
                    throws IOException {
                jsonGenerator.writeStartObject();
                for (Contributor contributor : team.contributors) {
                    jsonGenerator.writeFieldName(contributor.getFirstName());
                    serializerProvider.defaultSerializeValue(contributor, jsonGenerator);
                }

                jsonGenerator.writeEndObject();
            }
        });
    }

    @Validate
    public void start() {
        repository.register(module);
    }

    @Invalidate
    public void stop() {
        repository.unregister(module);
    }
}
