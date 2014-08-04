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
package controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.FormObject;
import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class FormIT extends WisdomBlackBoxTest {

    @Test
    public void testFormMapping() throws Exception {
        HttpResponse<String> response = post("/hello/form")
                .field("name", "wisdom")
                .field("email", "wisdom@wisdom-framework.org")
                .field("primInt", "593765")
                .field("objInt", "593766")
                .field("primLong", "-3957393")
                .field("objLong", "-3957394")
                .field("primFloat", "78.12")
                .field("objFloat", "79.22")
                .field("primDouble", "694.56")
                .field("objDouble", "696.76")
                .field("primBoolean", "false")
                .field("objBoolean", "true")
                .field("primByte", "111")
                .field("objByte", "112")
                .field("primShort", "32456")
                .field("objShort", "32455")
                .field("primChar", "Z")
                .field("objChar", "X")
                .asString();
        assertThat(response.code()).isEqualTo(OK);
        ObjectMapper mapper = new ObjectMapper();
        FormObject returnedObject = mapper.readValue(response.body(), FormObject.class);

        assertEquals("wisdom", returnedObject.name);
        assertEquals("wisdom@wisdom-framework.org", returnedObject.getEmail());

        assertEquals(593765, returnedObject.primInt);
        assertEquals(593766, returnedObject.objInt.intValue());

        assertEquals(-3957393, returnedObject.primLong);
        assertEquals(-3957394, returnedObject.objLong.longValue());

        assertEquals(78.12, returnedObject.primFloat, 0.001);
        assertEquals(79.22, returnedObject.objFloat, 0.001);

        assertEquals(694.56, returnedObject.primDouble, 0.001);
        assertEquals(696.76, returnedObject.objDouble, 0.001);

        assertEquals(false, returnedObject.isPrimBoolean());
        assertEquals(true, returnedObject.getObjBoolean());

        assertEquals(111, returnedObject.getPrimByte());
        assertEquals(112, returnedObject.getObjByte().byteValue());

        assertEquals(32456, returnedObject.getPrimShort());
        assertEquals(32455, returnedObject.getObjShort().shortValue());

        assertEquals('Z', returnedObject.getPrimChar());
        assertEquals('X', returnedObject.getObjChar().charValue());
    }
}
