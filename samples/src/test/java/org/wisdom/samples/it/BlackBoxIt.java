package org.wisdom.samples.it;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wisdom.test.WisdomBlackBoxRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Some blackbox tests.
 */
@RunWith(WisdomBlackBoxRunner.class)
public class BlackBoxIt {

    @Test
    public void testSamples() throws UnirestException {
        HttpResponse<String> response = Unirest.get("http://localhost:9000/samples").asString();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getBody().contains("samples"));
        final Document document = Jsoup.parse(response.getBody());
        assertThat(document.getElementsByTag("h1").get(0).text()).isEqualToIgnoringCase("samples");
    }

    @Test
    public void testHelloResults() throws UnirestException {
        HttpResponse<String> response = Unirest.post("http://localhost:9000/samples/hello/result")
                .field("name", "stuff")
                .asString();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getBody().contains("stuff"));
    }

    @Test
    public void testTODO() throws UnirestException, JSONException {
        // Get an empty list.
        HttpResponse<JsonNode> list = Unirest.get("http://localhost:9000/todo/tasks")
                .asJson();
        assertThat(list.getCode()).isEqualTo(200);
        assertThat(list.getBody().isArray());
        assertThat(list.getBody().getArray().length()).isEqualTo(0);

        // Post an item
        HttpResponse<JsonNode> create = Unirest.post("http://localhost:9000/todo/tasks")
                .field("name", "my todo")
                .asJson();
        assertThat(create.getCode()).isEqualTo(200);

        //Retrieve the new list
        list = Unirest.get("http://localhost:9000/todo/tasks")
                .asJson();
        assertThat(list.getCode()).isEqualTo(200);
        assertThat(list.getBody().getArray().length()).isEqualTo(1);
        assertThat(list.getBody().getArray().getJSONObject(0).getString("name")).contains("my todo");
        assertThat(list.getBody().getArray().getJSONObject(0).getBoolean("completed")).isFalse();
        final int id = list.getBody().getArray().getJSONObject(0).getInt("id");
        assertThat(id).isNotNull();

        // Set the item as completed
        HttpResponse<JsonNode> completion = Unirest.post("http://localhost:9000/todo/tasks/" + id)
                .field("completed", true)
                .asJson();
        assertThat(create.getCode()).isEqualTo(200);

        list = Unirest.get("http://localhost:9000/todo/tasks")
                .asJson();
        assertThat(list.getCode()).isEqualTo(200);
        assertThat(list.getBody().getArray().length()).isEqualTo(1);
        assertThat(list.getBody().getArray().getJSONObject(0).getBoolean("completed"));

    }

}
