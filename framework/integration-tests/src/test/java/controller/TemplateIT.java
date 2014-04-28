package controller;

import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateIT extends WisdomBlackBoxTest {

    @Test
    public void testEscaping() throws Exception {
        String expected = "&lt;script&gt;alert(&#39;Hello! &lt;&gt;&amp;&quot;&#39;&#39;);&lt;/script&gt;";
        HttpResponse<String> response = get("/templates/escaping").asString();
        assertThat(response.body()).contains(expected);
    }

}
