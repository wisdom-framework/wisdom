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
package org.wisdom.framework.filters.test;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.wisdom.api.http.Status;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


public class ProxyFilterIT extends WisdomBlackBoxTest {

    /**
     * Deploy the test bundle as we need the messages.
     */
    @BeforeClass
    public static void init() throws BundleException {
        installTestBundle();
    }

    @AfterClass
    public static void cleanup() throws BundleException {
        removeTestBundle();
    }

    @Test
    public void checkThatGetRequestAreProxied() throws Exception {
        HttpResponse<String> response = get("/proxy/xml").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body()).contains("<title>Overview</title>");
    }

    @Test
    public void checkThatRedirectedRequestAreProxied() throws Exception {
        HttpResponse<String> response = get("/proxy/redirect").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body()).contains("Perdu sur l'Internet ?");
    }

    @Test
    public void checkThatGetRequestWithQueryAreProxied() throws Exception {
        HttpResponse<JsonNode> response = get("/proxy/get").field("foo", "bar").field("count", 1).field("count", 2)
                .asJson();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body().get("args").get("count").get(0).asText()).isEqualTo("1");
        assertThat(response.body().get("args").get("count").get(1).asText()).isEqualTo("2");
        assertThat(response.body().get("args").get("foo").asText()).isEqualTo("bar");
    }

    @Test
    public void checkThatPostRequestAreProxied() throws Exception {
        HttpResponse<JsonNode> response = post("/proxy/post").body("Hello Wisdom").asJson();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body().get("data").asText()).isEqualTo("Hello Wisdom");
    }

    @Test
    public void checkThatPostWithoutDataRequestAreProxied() throws Exception {
        HttpResponse<JsonNode> response = post("/proxy/post").asJson();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body().get("data").asText()).isEqualTo("");
    }

    @Test
    public void checkThatPostRequestContainingFormDataAreProxied() throws Exception {
        HttpResponse<JsonNode> response = post("/proxy/post")
                .field("foo", "bar")
                .field("file", new File("src/test/resources/simple.txt"))
                .asJson();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body().get("files").get("file").asText()).contains("simple file");
        assertThat(response.body().get("form").get("foo").asText()).isEqualTo("bar");
    }

    @Test
    public void checkConfigurationSupport() throws Exception {
        HttpResponse<String> response = get("/proxy/xml2").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body()).contains("<title>Overview</title>");
    }

}
