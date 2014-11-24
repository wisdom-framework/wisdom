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
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class FilterIT extends WisdomBlackBoxTest {

    @Test
    public void testFilter() throws Exception {
        HttpResponse<JsonNode> response = get("/filter/dummy?insertValue=true").asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().has("key")).isTrue();
        assertThat(response.body().get("key").asText()).isEqualTo("value");
        assertThat(response.body().get("foo").asText()).isEqualTo("");
        assertThat(response.body().get("field").asText()).isEqualTo("");
        assertThat(response.header("X-Filtered")).isEqualTo("true");
    }

    @Test
    public void testThatValidationHappenAfterFiltering() throws Exception {
        HttpResponse<JsonNode> response = get("/filter/dummy?insertValue=false").asJson();
        assertThat(response.code()).isEqualTo(BAD_REQUEST);
        assertThat(response.body().has("key")).isFalse();
        assertThat(response.header("X-Filtered")).isEqualTo("true");
    }

    @Test
    public void testFilterModifyingValues() throws Exception {
        HttpResponse<JsonNode> response = get("/filter/dummy?insertValue=true&modifyValue=true").asJson();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body().has("key")).isTrue();
        assertThat(response.body().get("key").asText()).isEqualTo("value");
        assertThat(response.body().get("foo").asText()).isEqualTo("bar");
        assertThat(response.body().get("field").asText()).isEqualTo("value");
        assertThat(response.header("X-Filtered")).isEqualTo("true");
    }
}
