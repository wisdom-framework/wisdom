/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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

import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.assertions.WisdomAssertions.assertThat;

/**
 * Check routes using `accepts` and `consumes`
 */
public class NegotiationIT extends WisdomBlackBoxTest {


    @Test
    public void testRouteSelectionBasedOnAcceptHeader() throws Exception {
        HttpResponse<String> xml = get("/negotiation/")
                .header(ACCEPT, "application/xml")
                .asString();

        assertThat(xml.body()).contains("<hello>wisdom</hello>");
        assertThat(xml.header(VARY)).contains(ACCEPT);

        HttpResponse<String> json = get("/negotiation/")
                .header(ACCEPT, "application/json")
                .asString();
        assertThat(json.body()).contains("wisdom").contains("{").contains("}");
        assertThat(json.header(VARY)).contains(ACCEPT);

        HttpResponse<String> binary = get("/negotiation/")
                .header(ACCEPT, MimeTypes.BINARY)
                .asString();
        assertThat(binary).hasStatus(NOT_ACCEPTABLE);
    }

    @Test
    public void testRouteSelectionBasedOnContentTypeHeader() throws Exception {
        HttpResponse<String> xml = post("/negotiation/consume")
                .header(CONTENT_TYPE, "application/xml")
                .body("<hello>wisdom</hello>")
                .asString();

        assertThat(xml.body()).contains("<hello/>");
        assertThat(xml.header(VARY)).contains(CONTENT_TYPE);

        HttpResponse<String> json = post("/negotiation/consume")
                .header(CONTENT_TYPE, "application/json")
                .body("{\"hello\":\"wisdom\"}")
                .asString();
        assertThat(json.body()).contains("wisdom").contains("{").contains("}");
        assertThat(xml.header(VARY)).contains(CONTENT_TYPE);

        HttpResponse<String> binary = post("/negotiation/consume")
                .header(CONTENT_TYPE, MimeTypes.BINARY)
                .asString();
        assertThat(binary).hasStatus(UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testRouteSelectionBasedOnContentTypeAndAcceptHeaders() throws Exception {
        HttpResponse<String> xml = post("/negotiation/consprod")
                .header(CONTENT_TYPE, "application/xml")
                .body("<hello>wisdom</hello>")
                .asString();

        assertThat(xml.body()).contains("<hello/>");
        assertThat(xml.header(VARY)).contains(CONTENT_TYPE);

        HttpResponse<String> json = post("/negotiation/consprod")
                .header(CONTENT_TYPE, "application/json")
                .body("{\"hello\":\"wisdom\"}")
                .asString();
        assertThat(json.body()).contains("wisdom").contains("{").contains("}");
        assertThat(json.header(VARY)).contains(CONTENT_TYPE);

        HttpResponse<String> binary = post("/negotiation/consprod")
                .header(CONTENT_TYPE, MimeTypes.BINARY)
                .asString();
        assertThat(binary).hasStatus(UNSUPPORTED_MEDIA_TYPE);

        // Check when both Content Type and Accept are set
        HttpResponse<String> xml2 = post("/negotiation/consprod")
                .header(CONTENT_TYPE, "application/xml")
                .header(ACCEPT, "application/xml")
                .body("<hello>wisdom</hello>")
                .asString();

        assertThat(xml2.body()).contains("<hello/>");
        assertThat(xml2.header(VARY)).contains(CONTENT_TYPE);

        HttpResponse<String> json2 = post("/negotiation/consprod")
                .header(CONTENT_TYPE, "application/json")
                .header(ACCEPT, "application/json")
                .body("{\"hello\":\"wisdom\"}")
                .asString();
        assertThat(json2.body()).contains("wisdom").contains("{").contains("}");
        assertThat(json2.header(VARY)).contains(CONTENT_TYPE);

        HttpResponse<String> binary2 = post("/negotiation/consprod")
                .header(CONTENT_TYPE, "application/json")
                .header(ACCEPT, "application/binary")
                .body("{\"hello\":\"wisdom\"}")
                .asString();
        assertThat(binary2).hasStatus(NOT_ACCEPTABLE);

    }


}
