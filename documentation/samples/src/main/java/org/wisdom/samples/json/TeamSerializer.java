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
package org.wisdom.samples.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.*;
import org.wisdom.api.annotations.Service;
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
@Service(Module.class)
public class TeamSerializer extends SimpleModule {


    public TeamSerializer() {
        super("TeamSerializer");
        addSerializer(Team.class, new JsonSerializer<Team>() {
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
}
