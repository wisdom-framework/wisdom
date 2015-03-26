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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericIT extends WisdomBlackBoxTest {


    @Test
    public void testJson() throws Exception {
        HttpResponse<JsonNode> response = post("/generic/1")
                .header(CONTENT_TYPE, MimeTypes.JSON)
                .body("{\"nested\":{\"foo\":\"hello\"}}")
                .asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().toString()).contains("hello");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");

        response = post("/generic/2")
                .header(CONTENT_TYPE, MimeTypes.JSON)
                .body("{\"nested\":{\"foo\":1}}")
                .asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().toString()).contains("1");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

    @Test
    public void testXml() throws Exception {
        HttpResponse<String> response = post("/generic/1")
                .header(CONTENT_TYPE, MimeTypes.XML)
                .body("<DataWrapper>\n" +
                        "     <nested>\n" +
                        "     <foo>hello</foo>\n" +
                        "     </nested>\n" +
                        "     </DataWrapper>")
                .asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("hello");
        assertThat(response.contentType()).isEqualTo(MimeTypes.XML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");

        response = post("/generic/2")
                .header(CONTENT_TYPE, MimeTypes.XML)
                .body("<DataWrapper>\n" +
                        "     <nested>\n" +
                        "     <foo>1</foo>\n" +
                        "     </nested>\n" +
                        "     </DataWrapper>")
                .asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("1");
        assertThat(response.contentType()).isEqualTo(MimeTypes.XML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }



}
