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
package content;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Ignore;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FormIT extends WisdomBlackBoxTest {

    @Test
    public void testFormUploadUsingUrlEncoding() throws Exception {
        HttpResponse<JsonNode> response = post("/content/form/3")
                .field("name", "wisdom")
                .field("age", "1")
                .asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.body().get("content-type").asText()).contains(MimeTypes.FORM);
        assertThat(response.body().get("content").get("name").asText()).isEqualTo("wisdom");
        assertThat(response.body().get("content").get("age").asInt()).isEqualTo(1);
        assertThat(response.body().get("content").get("content").asInt()).isEqualTo(0);
        assertThat(response.body().get("content").get("id").asInt()).isEqualTo(3);
    }

    @Test
    public void testFormUploadUsingMultipart() throws Exception {
        final File file = new File("src/test/resources/a_file.txt");
        assertThat(file).exists();
        HttpResponse<JsonNode> response = post("/content/form/3")
                .field("name", "wisdom")
                .field("age", "1")
                .field("content", file)
                .asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.JSON);
        assertThat(response.body().get("content-type").asText()).contains(MimeTypes.MULTIPART);
        assertThat(response.body().get("content").get("name").asText()).isEqualTo("wisdom");
        assertThat(response.body().get("content").get("age").asInt()).isEqualTo(1);
        assertThat(response.body().get("content").get("content").asInt()).isEqualTo(40);
        assertThat(response.body().get("content").get("id").asInt()).isEqualTo(3);
    }

}
