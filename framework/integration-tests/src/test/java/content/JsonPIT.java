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

import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPIT extends WisdomBlackBoxTest {

    @Test
    public void testJsonPUsingRenderable() throws Exception {
        HttpResponse<String> response = get("/jsonp/render?callback=baz").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("baz({").contains("\"foo\" : \"bar\"").contains("});");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JAVASCRIPT);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

    @Test
    public void testJsonPUsingJsonService() throws Exception {
        HttpResponse<String> response = get("/jsonp/json?callback=baz").asString();
        assertThat(response.code()).isEqualTo(OK);
        System.out.println(response.body());
        assertThat(response.body()).contains("baz({").contains("\"foo\" : \"bar\"").contains("});");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JAVASCRIPT);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

    @Test
    public void testJsonPUsingJsonServiceAndMapper() throws Exception {
        HttpResponse<String> response = get("/jsonp/user?callback=baz").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("baz({")
                .contains("\"id\" : 1,")
                .contains("\"name\" : \"wisdom\",")
                .contains("\"favorites\" : [ \"coffee\", " +
                        "\"whisky\" ]")
                .contains("});");
        assertThat(response.contentType()).isEqualTo(MimeTypes.JAVASCRIPT);
        assertThat(response.charset()).isEqualToIgnoringCase("UTF-8");
    }

}
