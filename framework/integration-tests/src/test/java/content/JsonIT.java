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

public class JsonIT extends WisdomBlackBoxTest {

    @Test
    public void testJsonWithContentTypeAndCharset() throws Exception {
        HttpResponse<JsonNode> response = get("/json/jsonWithContentTypeAndCharset").asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().toString()).isEqualTo("{}");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

    @Test
    public void testJsonWithContentType() throws Exception {
        HttpResponse<JsonNode> response = get("/json/jsonWithContentType").header(ACCEPT, MimeTypes.JSON).asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().toString()).contains("accept").contains(MimeTypes.JSON);
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

    @Test
    public void testStructureMapping() throws Exception {
        HttpResponse<JsonNode> response = get("/json/user").asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.body().toString()).contains("{\"id\":1,\"name\":\"wisdom\",\"favorites\":[\"coffee\"," +
                "\"whisky\"]}");
    }

    @Test
    public void testBodyParsing() throws Exception {
        HttpResponse<JsonNode> response = post("/json/post1").header(CONTENT_TYPE,
                MimeTypes.JSON).body("{\"foo\":\"bar\"}").asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().get("foo").asText()).isEqualTo("bar");

        response = post("/json/post2")
                .header(CONTENT_TYPE, MimeTypes.JSON)
                .body("{\"foo\":\"bar\"}")
                .asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().get("foo").asText()).isEqualTo("bar");
    }

    /**
     * Related to #187.
     */
    @Test
    public void testRawStringInOk() throws Exception {
        HttpResponse<JsonNode> response = get("/json/simple").asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().toString()).isEqualTo("\"wisdom\"");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

}
