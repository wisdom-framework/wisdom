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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Status;
import org.wisdom.test.http.HttpRequestWithBody;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

public class CorsFilterIT extends WisdomBlackBoxTest {

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
    public void checkThatHeadersAreAddedIfPostRouteExists() throws Exception {
        HttpResponse<String> response = post("/corsTests/post").header(ORIGIN, "http://localhost").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
    }

    @Test
    public void checkThatHeadersAreNotAddedIfNoOriginHeader() throws Exception {
        HttpResponse<String> response = post("/corsTests/post").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    public void checkThatPreflightWorksWithPost() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/post"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
    }

    @Test
    public void checkThatPreflightReturnsNotFoundWithoutOriginHeader() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/post"))
                .asString();
        assertThat(response.code()).isEqualTo(Status.NOT_FOUND);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    public void checkThatHeadersAreAddedIfGetRouteExists() throws Exception {
        HttpResponse<String> response = get("/corsTests/get").header(ORIGIN, "http://localhost")
                .header(ACCESS_CONTROL_REQUEST_METHOD, "GET").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
    }

    @Test
    public void checkThatPreflightFailsIfWrongAccessControlRequestMethodHeader() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/get"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.UNAUTHORIZED);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
        assertThat(response.header(ACCESS_CONTROL_ALLOW_METHODS)).contains("GET");
    }

    @Test
    public void checkThatPreflightWorksWithGet() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/get"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "GET").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
        assertThat(response.header(ACCESS_CONTROL_ALLOW_METHODS)).isNotNull().contains("GET");
    }

    @Test
    public void checkThatHeadersAreAddedIfPutRouteExists() throws Exception {
        HttpResponse<String> response = put("/corsTests/put").header(ORIGIN, "http://localhost").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
    }

    @Test
    public void checkThatPreflightWorksWithPut() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/put"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "PUT").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
        assertThat(response.header(ACCESS_CONTROL_ALLOW_METHODS)).isNotNull().contains("PUT");
    }

    @Test
    public void checkThatPreflightWorksWithMultipleVerbs() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/postPutGet"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "PUT").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
        assertThat(response.header(ACCESS_CONTROL_ALLOW_METHODS)).isNotNull().contains("PUT", "POST", "GET");
    }

    @Test
    public void checkThatUnboundRoutesAreTheSame() throws Exception {
        HttpResponse<String> response = post("/corsTests/unbound").header(ORIGIN, "http://localhost").asString();
        assertThat(response.code()).isEqualTo(Status.NOT_FOUND);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
        assertThat(response.header(ACCESS_CONTROL_ALLOW_HEADERS)).isNull();

        response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/unbound"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.NOT_FOUND);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
        assertThat(response.header(ACCESS_CONTROL_ALLOW_HEADERS)).isNull();

        response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/unbound")).asString();
        assertThat(response.code()).isEqualTo(Status.NOT_FOUND);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
        assertThat(response.header(ACCESS_CONTROL_ALLOW_HEADERS)).isNull();
    }

    @Test
    public void checkThatPreflightWorksWithDynamicRoutes() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS,
                getHttpURl("/corsTests/dynamic/test")).header(ORIGIN, "http://localhost")
                .header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_ORIGIN)).isNotNull().contains("http://localhost");
        assertThat(response.header(ACCESS_CONTROL_ALLOW_METHODS)).isNotNull().contains("POST");
    }

    @Test
    public void checkThatMaxAgeHeaderIsPresent() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/post"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_MAX_AGE)).isNotNull().contains("86400");
    }

    @Test
    public void checkThatAllowCredentialsHeaderIsPresent() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/post"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_CREDENTIALS)).isNotNull().contains("true");
    }

    @Test
    public void checkThatAllowHeaderIsPresent() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/post"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_HEADERS)).isNotNull().contains("X-Custom-Header");
    }

    @Test
    public void checkThatPreflightAllowHeaderIsPresent() throws Exception {
        HttpResponse<String> response = new HttpRequestWithBody(HttpMethod.OPTIONS, getHttpURl("/corsTests/post"))
                .header(ORIGIN, "http://localhost").header(ACCESS_CONTROL_REQUEST_METHOD, "POST").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_ALLOW_HEADERS)).isNotNull().contains("X-Custom-Header");
    }

    @Test
    public void checkThatHeadersAreExposedIfGetRouteExists() throws Exception {
        HttpResponse<String> response = get("/corsTests/get").header(ORIGIN, "http://localhost")
                .header(ACCESS_CONTROL_REQUEST_METHOD, "GET").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.header(ACCESS_CONTROL_EXPOSE_HEADERS)).isNotNull().contains("X-Custom-Header");
    }
}
