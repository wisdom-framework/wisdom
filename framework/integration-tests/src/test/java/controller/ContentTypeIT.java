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

import org.junit.BeforeClass;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.http.Options;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentTypeIT extends WisdomBlackBoxTest {

    @BeforeClass
    public static void refresh() {
        Options.refresh();
    }

    @Test
    public void testPlainText() throws Exception {
        HttpResponse<String> response = get("/hello/plain").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("Hello World");
        assertThat(response.contentType()).isEqualTo(MimeTypes.TEXT);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.length()).isEqualTo("Hello World".length());
    }

    @Test
    public void testHTML() throws Exception {
        HttpResponse<String> response = get("/hello/html").noEncoding().asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("<h1>Hello World</h1>");
        assertThat(response.contentType()).isEqualTo(MimeTypes.HTML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.length()).isEqualTo("<h1>Hello World</h1>".length());
    }

    @Test
    public void testJSON() throws Exception {
        HttpResponse<String> response = get("/hello/json").noEncoding().asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("{\"message\":\"Hello World\"}");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.length()).isEqualTo("{\"message\":\"Hello World\"}".length());
    }

    @Test
    public void testXML() throws Exception {
        HttpResponse<String> response = get("/hello/xml").noEncoding().asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("<message>Hello World</message>");
        assertThat(response.contentType()).isEqualTo(MimeTypes.XML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.length()).isEqualTo("<message>Hello World</message>".length());
    }

    @Test
    public void testJSONMapping() throws Exception {
        HttpResponse<String> response = get("/hello/json/mapping").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("{\"message\":\"Hello World\"}");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.length()).isEqualTo("{\"message\":\"Hello World\"}".length());
    }

    @Test
    public void testXMLMapping() throws Exception {
        HttpResponse<String> response = get("/hello/xml").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("<message>Hello World</message>");
        assertThat(response.contentType()).isEqualTo(MimeTypes.XML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
        assertThat(response.length()).isEqualTo("<message>Hello World</message>".length());
    }

    @Test
    public void testJSONNode() throws Exception {
        HttpResponse<String> response = get("/hello/json/node").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("\"message\" : \"Hello World\"");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

    @Test
    public void testXMLDocument() throws Exception {
        HttpResponse<String> response = get("/hello/xml/node").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("<message>Hello World</message>");
        assertThat(response.contentType()).isEqualTo(MimeTypes.XML);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

    @Test
    public void testAccept() throws Exception {
        HttpResponse<String> response = get("/hello/accept").header(ACCEPT, "text/html,application/xml;q=0").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("html");

        response = get("/hello/accept").header(ACCEPT, "text/*").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("html");

        response = get("/hello/accept").header(ACCEPT, "application/json").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("json");
    }

    @Test
    public void testAcceptBasedNegotiationUsingAsync() throws Exception {
        HttpResponse<String> response = get("/hello/negotiation/accept").header(ACCEPT, "text/html").asStringAsync()
                .get(1, TimeUnit.MINUTES);
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.HTML);
        assertThat(response.header(VARY)).isEqualTo(ACCEPT);
        assertThat(response.body()).contains("<h1>Hello</h1>");

        response = get("/hello/negotiation/accept").header(ACCEPT, "text/html;q=0.5, application/*").asStringAsync()
                .get(1, TimeUnit.MINUTES);
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.header(VARY)).isEqualTo(ACCEPT);
        assertThat(response.body()).contains("hello").contains("message");

        response = get("/hello/negotiation/accept").header(ACCEPT, "application/xml").asStringAsync()
                .get(1, TimeUnit.MINUTES);
        assertThat(response.code()).isEqualTo(NOT_ACCEPTABLE);
        assertThat(response.header(VARY)).isNull();

        // No Accept header.
        response = get("/hello/negotiation/accept").asStringAsync().get(1, TimeUnit.MINUTES);
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.HTML);
        assertThat(response.header(VARY)).isEqualTo(ACCEPT);
        assertThat(response.body()).contains("<h1>Hello</h1>");
    }

    @Test
    public void testAcceptBasedNegotiationSync() throws Exception {
        HttpResponse<String> response = get("/hello/negotiation/accept/sync").header(ACCEPT, "text/html").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.HTML);
        assertThat(response.header(VARY)).isEqualTo(ACCEPT);
        assertThat(response.body()).contains("<h1>Hello</h1>");

        response = get("/hello/negotiation/accept/sync").header(ACCEPT, "text/html;q=0.5, application/*").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.header(VARY)).isEqualTo(ACCEPT);
        assertThat(response.body()).contains("hello").contains("message");

        response = get("/hello/negotiation/accept/sync").header(ACCEPT, "application/xml").asString();
        assertThat(response.code()).isEqualTo(NOT_ACCEPTABLE);
        assertThat(response.header(VARY)).isNull();

        // No Accept header.
        response = get("/hello/negotiation/accept/sync").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.HTML);
        assertThat(response.header(VARY)).isEqualTo(ACCEPT);
        assertThat(response.body()).contains("<h1>Hello</h1>");
    }

}
