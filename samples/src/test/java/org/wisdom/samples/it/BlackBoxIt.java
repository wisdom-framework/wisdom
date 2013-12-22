package org.wisdom.samples.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Some blackbox tests.
 */
public class BlackBoxIt extends WisdomBlackBoxTest {

    @Test
    public void testSamples() throws Exception {
        HttpResponse<Document> response = get("samples").asHtml();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().getElementsByTag("h1").get(0).text()).isEqualToIgnoringCase("samples");
    }

    @Test
    public void testHelloResults() throws Exception {
        HttpResponse<Document> response = post("samples/hello/result")
                .field("name", "stuff")
                .asHtml();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().text().contains("stuff"));
    }

    @Test
    public void testTODO() throws Exception {
        // Get an empty list.
        HttpResponse<JsonNode> list = get("/todo/tasks")
                .asJson();
        assertThat(list.code()).isEqualTo(OK);
        assertThat(list.body().isArray());
        assertThat(list.body().size()).isEqualTo(0);

        // Post an item
        HttpResponse<JsonNode> create = post("/todo/tasks")
                .field("name", "my todo")
                .asJson();
        assertThat(create.code()).isEqualTo(OK);

        //Retrieve the new list
        list = get("/todo/tasks").asJson();
        assertThat(list.code()).isEqualTo(OK);
        assertThat(list.body().size()).isEqualTo(1);
        assertThat(list.body().get(0).get("name").asText()).contains("my todo");
        assertThat(list.body().get(0).get("completed").asBoolean()).isFalse();
        final int id = list.body().get(0).get("id").asInt();
        assertThat(id).isNotNull();

        // Set the item as completed
        HttpResponse<JsonNode> completion = post("todo/tasks/" + id)
                .field("completed", true)
                .asJson();
        assertThat(create.code()).isEqualTo(OK);

        list = get("todo/tasks").asJson();
        assertThat(list.code()).isEqualTo(OK);
        assertThat(list.body().size()).isEqualTo(1);
        assertThat(list.body().get(0).get("completed").asBoolean());
    }

}
