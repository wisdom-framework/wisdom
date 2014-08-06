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

import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class XMLIT extends WisdomBlackBoxTest {

    @Test
    public void testBodyParsing() throws Exception {
        HttpResponse<String> response = post("/xml/post1").header(CONTENT_TYPE,
                MimeTypes.XML).body("<element>name</element>").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("<element>").contains("name").contains("</element>");

        response = post("/xml/post2").header(CONTENT_TYPE,
                MimeTypes.XML).body("<element>name</element>").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("<element>").contains("name").contains("</element>");

        // Test alternative mime types
        response = post("/xml/post1").header(CONTENT_TYPE,
                "text/xml").body("<element>name</element>").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("<element>").contains("name").contains("</element>");

        response = post("/xml/post1").header(CONTENT_TYPE,
                "application/atom+xml").body("<element>name</element>").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("<element>").contains("name").contains("</element>");

        response = post("/xml/post2").header(CONTENT_TYPE,
                "text/xml").body("<element>name</element>").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("<element>").contains("name").contains("</element>");

        response = post("/xml/post2").header(CONTENT_TYPE,
                "application/atom+xml").body("<element>name</element>").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("<element>").contains("name").contains("</element>");
    }

    @Test
    public void testStructureMapping() throws Exception {
        HttpResponse<String> response = get("/xml/user").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.XML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.body()).contains("<User").contains("<id>1</id>").contains("<favorites>").contains
                ("<favorites>coffee</favorites>").contains("</User>");
    }
    /**
     * Related to #187.
     */
    @Test
    public void testRawStringInOk() throws Exception {
        HttpResponse<String> response = get("/xml/simple").asString();
        assertThat(response.code()).isEqualTo(OK);
        // Weird result, but it's what we want.
        assertThat(response.body()).contains("<String xmlns=\"\">wisdom</String>");
        assertThat(response.contentType()).isEqualTo(MimeTypes.XML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }


}
