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
package content;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check posting XML and JSON.
 */
public class PersonControllerIT extends WisdomBlackBoxTest {

    @Test
    public void testPostPersonAsJson() throws Exception {
        String name = "zeeess name - and some utf8 => öäü";
        String json = "{\"name\":\"" + name + "\"}";
        HttpResponse<JsonNode> response = post("/person/json").header(CONTENT_TYPE, MimeTypes.JSON).body(json).asJson();
        assertThat(response.body().get("name").asText()).isEqualTo(name);
    }

    @Test
    public void testPostPersonAsXML() throws Exception {
        String name = "zeeess name - and some utf8 => öäü";
        HttpResponse<String> response = post("/person/xml").header(CONTENT_TYPE, MimeTypes.XML).body("<Person><name>" +
                name + "</name></Person>").asString();
        System.out.println(response.body());
        assertThat(response.body()).contains(name.replace(">", "&gt;"));
    }

    @Test
    public void testPostPersonWithAcceptSetToJson() throws Exception {
        String name = "zeeess name - and some utf8 => öäü";
        String json = "{\"name\":\"" + name + "\"}";
        HttpResponse<JsonNode> response = post("/person/accept")
                .header(CONTENT_TYPE, MimeTypes.JSON)
                .header(ACCEPT, MimeTypes.JSON)
                .body(json).asJson();
        assertThat(response.body().get("name").asText()).isEqualTo(name);
    }

    @Test
    public void testPostPersonWithAcceptSetToXML() throws Exception {
        String name = "zeeess name - and some utf8 => öäü";
        HttpResponse<String> response = post("/person/accept")
                .header(CONTENT_TYPE, MimeTypes.XML)
                .header(ACCEPT, MimeTypes.XML)
                .body("<Person><name>" + name + "</name></Person>").asString();
        System.out.println(response.body());
        assertThat(response.body()).contains(name.replace(">", "&gt;"));
    }

    @Test
    public void testPostPersonWithAcceptSetToText() throws Exception {
        String name = "zeeess name - and some utf8 => öäü";
        String json = "{\"name\":\"" + name + "\"}";
        HttpResponse<String> response = post("/person/accept")
                .header(CONTENT_TYPE, MimeTypes.JSON)
                .header(ACCEPT, MimeTypes.TEXT)
                .body(json).asString();
        // No serializer, calling toString on the content.
        assertThat(response.body()).isEqualTo("my name is zeeess name - and some utf8 => öäü");
    }


}
