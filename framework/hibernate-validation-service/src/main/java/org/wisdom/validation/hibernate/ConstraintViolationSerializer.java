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
package org.wisdom.validation.hibernate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;

import java.io.IOException;

/**
 * Serializes validation constraints.
 * Violation Constraints from Hibernate Validator have a cycle, so need a serializer.
 * <p/>
 * Each violation is then sent as a JSON object with the following structure:
 * {message="message" [, invalid="the invalid value if any"]}
 */
public class ConstraintViolationSerializer extends JsonSerializer<ConstraintViolationImpl> {

    /**
     * @return the serialized class.
     */
    @Override
    public Class<ConstraintViolationImpl> handledType() {
        return ConstraintViolationImpl.class;
    }

    /**
     * Writes the JSON form of the Constraint Violation.
     *
     * @param constraintViolation the violation
     * @param jsonGenerator       the generator
     * @param serializerProvider  the provider
     * @throws IOException if it cannot be serialized
     */
    @Override
    public void serialize(ConstraintViolationImpl constraintViolation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("message", constraintViolation.getMessage());
        Object invalidValue = constraintViolation.getInvalidValue();
        if (invalidValue != null) {
            jsonGenerator.writeStringField("invalid", invalidValue.toString());
        }
        if (constraintViolation.getPropertyPath() != null) {
            jsonGenerator.writeStringField("path", constraintViolation.getPropertyPath().toString());
        }
        jsonGenerator.writeEndObject();
    }
}
