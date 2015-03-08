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
package org.wisdom.framework.csrf.it;

import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Status;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;


public class CSRFIT extends WisdomBlackBoxTest {

    @BeforeClass
    public static void init() throws BundleException {
        installTestBundle();
    }

    @AfterClass
    public static void cleanup() throws BundleException {
        removeTestBundle();
    }

    @Test
    public void testThatTheFormHaveTheTokenInjected() throws Exception {
        final HttpResponse<Document> response = get("/csrf").asHtml();
        assertThat(response.code()).isEqualTo(200);
        String token = response.body().select("#csrf_token").attr("value");
        assertThat(token).isNotEmpty();
    }

    @Test
    public void testThatValidRequestsAreAccepted() throws Exception {
        final HttpResponse<Document> response = get("/csrf").asHtml();
        assertThat(response.code()).isEqualTo(200);
        String token = response.body().select("#csrf_token").attr("value");
        assertThat(token).isNotEmpty();

        // Submit the form
        final HttpResponse<String> response2 =
                post("/csrf")
                        .header(HeaderNames.CONTENT_TYPE, "multipart/form-data")
                        .field("key", "hello").field("csrf_token", token).asString();
        assertThat(response2.code()).isEqualTo(200);
        assertThat(response2.body()).isEqualTo("hello");
    }

    @Test
    public void testThatInvalidRequestAreRejected() throws Exception {
        final HttpResponse<Document> response = get("/csrf").asHtml();
        assertThat(response.code()).isEqualTo(200);
        String token = response.body().select("#csrf_token").attr("value");
        assertThat(token).isNotEmpty();

        final HttpResponse<String> response2 =
                post("/csrf")
                        .header(HeaderNames.CONTENT_TYPE, "multipart/form-data")
                        .field("key", "hello").asString();
        assertThat(response2.code()).isEqualTo(Status.FORBIDDEN);
    }

    @Test
    public void testThatRequestWithInvalidRequestAreRejected() throws Exception {
        final HttpResponse<Document> response = get("/csrf").asHtml();
        assertThat(response.code()).isEqualTo(200);
        String token = response.body().select("#csrf_token").attr("value");
        assertThat(token).isNotEmpty();

        final HttpResponse<String> response2 =
                post("/csrf")
                        .header(HeaderNames.CONTENT_TYPE, "multipart/form-data")
                        .field("key", "hello").field("csrf_token", "not a token").asString();
        assertThat(response2.code()).isEqualTo(Status.FORBIDDEN);
    }

    @Test
    public void testThatTheCSRFDialectInjectsAToken() throws Exception {
        final HttpResponse<Document> response = get("/csrf/dialect").asHtml();
        assertThat(response.code()).isEqualTo(200);
        String token = response.body().select("input[type=hidden]").attr("value");
        assertThat(token).isNotEmpty();

        // Submit the form
        final HttpResponse<String> response2 =
                post("/csrf")
                        .header(HeaderNames.CONTENT_TYPE, "multipart/form-data")
                        .field("key", "hello").field("csrf_token", token).asString();
        assertThat(response2.code()).isEqualTo(200);
        assertThat(response2.body()).isEqualTo("hello");
    }

}
