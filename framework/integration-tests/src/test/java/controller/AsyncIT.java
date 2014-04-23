package controller;


import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncIT extends WisdomBlackBoxTest {

    @Test
    public void testSimpleAsync() throws Exception {
        HttpResponse<String> response = get("/hello/async/simple").asString();
        assertThat(response.body()).isEqualTo("x");
        assertThat(response.code()).isEqualTo(OK);
    }
}
