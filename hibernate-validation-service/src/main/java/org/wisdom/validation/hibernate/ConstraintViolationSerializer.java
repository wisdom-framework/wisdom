package org.wisdom.validation.hibernate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;

import java.io.IOException;

/**
 * Serializes validation constraints.
 */
public class ConstraintViolationSerializer extends JsonSerializer<ConstraintViolationImpl> {

    @Override
    public Class<ConstraintViolationImpl> handledType() {
        return ConstraintViolationImpl.class;
    }

    @Override
    public void serialize(ConstraintViolationImpl constraintViolation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("message", constraintViolation.getMessage());
        Object invalidValue = constraintViolation.getInvalidValue();
        if (invalidValue != null) {
            jsonGenerator.writeStringField("invalid", invalidValue.toString());
        }
        jsonGenerator.writeEndObject();
    }
}
