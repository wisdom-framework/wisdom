package org.wisdom.test.http;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Status;

import java.io.File;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the HTTP APi using HTTPBin (http://httpbin.org/), a kind of snoop HTTP service.
 */
public class HttpApiTest implements Status {

    @Test
    public void testRequests() throws  Exception {
        HttpResponse<JsonNode> jsonResponse = new HttpRequestWithBody(HttpMethod.POST, "http://httpbin.org/post")
                .header("accept", "application/json")
                .field("param1", "value1")
                .field("param2","bye")
                .asJson();

        assertThat(jsonResponse.headers().size() > 0);
        assertThat(jsonResponse.body().toString().length() > 0);
        assertThat(jsonResponse.raw() == null).isFalse();
        assertThat(jsonResponse.code()).isEqualTo(OK);

        JsonNode json = jsonResponse.body();
        assertThat(json.isArray()).isFalse();
        assertThat(json.isObject());
    }

    @Test
    public void testGet() throws  Exception {
        HttpResponse<JsonNode> response = new GetRequest("http://httpbin.org/get?name=mark").asJson();
        assertThat(response.body().get("args").get("name").asText()).isEqualToIgnoringCase("mark");

        response = new GetRequest("http://httpbin.org/get").field("name", "mark2").asJson();
        assertThat(response.body().get("args").get("name").asText()).isEqualToIgnoringCase("mark2");
    }

    @Test
    public void testGetMultiple() throws  Exception {
        for(int i=1;i<=20;i++) {
            HttpResponse<JsonNode> response = new GetRequest("http://httpbin.org/get?try=" + i).asJson();
            assertThat(response.body().get("args").get("try").asInt()).isEqualTo (i);
        }
    }

    @Test
    public void testGetFields() throws  Exception {
        HttpResponse<JsonNode> response = new GetRequest("http://httpbin.org/get")
                .field("name", "wisdom").field("test", "test").asJson();
        assertThat(response.body().get("args").get("name").asText()).isEqualToIgnoringCase("wisdom");
        assertThat(response.body().get("args").get("test").asText()).isEqualToIgnoringCase("test");
    }

    @Test
    public void testDelete() throws  Exception {
        HttpResponse<JsonNode> response = new HttpRequestWithBody(HttpMethod.DELETE,"http://httpbin.org/delete").asJson();
        assertThat(response.code()).isEqualTo(OK);

        response = new HttpRequestWithBody(HttpMethod.DELETE,"http://httpbin.org/delete").field("name", "mark").asJson();
        assertThat(response.body().get("data").asText()).isEqualToIgnoringCase("name=mark");
    }

    @Test
    public void testDeleteBody() throws  Exception {
        String body = "{\"jsonString\":{\"members\":\"members1\"}}";
        HttpResponse<JsonNode> response = new HttpRequestWithBody(HttpMethod.DELETE,"http://httpbin.org/delete")
                .body(body).asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().get("json").get("jsonString").get("members").asText()).isEqualTo("members1");
    }

    @Test
    public void testBasicAuth() throws  Exception {
        HttpResponse<JsonNode> response = new GetRequest("http://httpbin.org/headers").basicAuth("user", "test").asJson();
        assertThat(response.body().get("headers").get("Authorization").asText()).isEqualTo("Basic dXNlcjp0ZXN0");
    }

    @Test
    public void testAsync() throws  Exception {
        Future<HttpResponse<JsonNode>> future = new HttpRequestWithBody(HttpMethod.POST, "http://httpbin.org/post")
                .header("accept", "application/json")
                .field("param1", "value1")
                .field("param2","bye")
                .asJsonAsync();

        assertThat(future).isNotNull();
        HttpResponse<JsonNode> jsonResponse = future.get();

        assertThat(jsonResponse.headers().size() > 0);
        assertThat(jsonResponse.body().toString().length() > 0);
        assertThat(jsonResponse.raw() == null).isFalse();
        assertThat(jsonResponse.code()).isEqualTo(OK);

        JsonNode json = jsonResponse.body();
        assertThat(json.isArray()).isFalse();
        assertThat(json.isObject());
    }

    @Test
    public void testMultipart() throws  Exception {
        HttpResponse<JsonNode> jsonResponse =
                new HttpRequestWithBody(HttpMethod.POST, "http://httpbin.org/post")
                        .field("file", new File(getClass().getResource("/foo.txt").toURI())).asJson();
        assertThat(jsonResponse.headers().size() > 0);
        assertThat(jsonResponse.body().toString().length() > 0);
        assertThat(jsonResponse.code()).isEqualTo(OK);
        assertThat(jsonResponse.body().get("files").get("file").asText()).isEqualTo("A text file used in test.");
    }

    @Test
    public void testGzip() throws Exception {
        HttpResponse<JsonNode> jsonResponse =
                new GetRequest("http://httpbin.org/gzip").asJson();
        assertThat(jsonResponse.headers().size() > 0);
        assertThat(jsonResponse.body().asText().length() > 0);
        assertThat(jsonResponse.code()).isEqualTo(OK);

        JsonNode json = jsonResponse.body();
        assertThat(json.get("gzipped").asBoolean());
    }

    @Test
    public void testGzipAsync() throws Exception {
        HttpResponse<JsonNode> jsonResponse =
                new GetRequest("http://httpbin.org/gzip").asJsonAsync().get();
        assertThat(jsonResponse.headers().size() > 0);
        assertThat(jsonResponse.body().toString().length() > 0);

        assertThat(jsonResponse.code()).isEqualTo(OK);
        JsonNode json = jsonResponse.body();
        assertThat(json.get("gzipped").asBoolean());
    }
}
