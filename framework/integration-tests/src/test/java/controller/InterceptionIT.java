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
package controller;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.wisdom.api.content.Json;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class InterceptionIT extends WisdomBlackBoxTest {

    @Test
    public void testInterception() throws Exception {
        HttpResponse<JsonNode> response = get("/interception/my").asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().has("url")).isTrue();
        assertThat(response.body().has("message")).isTrue();
        assertThat(response.body().get("url").asText()).isEqualTo("http://perdu.com");
        assertThat(response.body().get("message").asText()).isEqualTo("hello wisdom");
    }
}
