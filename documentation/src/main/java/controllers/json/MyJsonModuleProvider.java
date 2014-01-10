package controllers.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.*;
import org.wisdom.api.content.JacksonModuleRepository;

import java.io.IOException;

/**
 * An example of Jackson module provider.
 */
@Component
@Instantiate
public class MyJsonModuleProvider {

    @Requires
    JacksonModuleRepository repository;                                 // <1>

    private final SimpleModule module;

    public MyJsonModuleProvider() {                                     // <2>
        module = new SimpleModule("My Json Module");
        module.addSerializer(Car.class, new JsonSerializer<Car>() {
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
        module.addDeserializer(Car.class, new JsonDeserializer<Car>() {
            @Override
            public Car deserialize(JsonParser jsonParser,
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

    @Validate
    public void start() {
        repository.register(module);                                    // <3>
    }

    @Invalidate
    public void stop() {
        repository.unregister(module);                                  // <4>
    }


}
