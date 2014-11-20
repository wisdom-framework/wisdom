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
// tag::controller[]
package snippets.controllers.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.wisdom.api.annotations.Service;

import java.io.IOException;

/**
 * An example of Module service to customize the JSON serialization and de-serialization process.
 */
@Service(Module.class)                                                  //<1>
public class MyJsonModuleService extends SimpleModule {


    public MyJsonModuleService() {
        super("My Json Module");                                       // <2>
        addSerializer(Car.class, new JsonSerializer<Car>() {           // <3>
            @Override
            public void serialize(Car car, JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider)
                    throws IOException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("custom", "customized value");
                jsonGenerator.writeStringField("color", car.getColor());
                jsonGenerator.writeStringField("brand", car.getBrand());
                jsonGenerator.writeStringField("model", car.getModel());
                jsonGenerator.writeNumberField("power", car.getPower());
                jsonGenerator.writeEndObject();
            }
        });
        addDeserializer(Car.class, new JsonDeserializer<Car>() {       // <4>
            @Override
            public Car deserialize(
                    JsonParser jsonParser,
                    DeserializationContext deserializationContext)
                    throws IOException {
                ObjectCodec oc = jsonParser.getCodec();
                JsonNode node = oc.readTree(jsonParser);
                String color = node.get("color").asText();
                String brand = node.get("brand").asText();
                String model = node.get("model").asText();
                int power = node.get("power").asInt();
                return new Car(brand, model, power, color);
            }
        });
    }
}
// end::controller[]
