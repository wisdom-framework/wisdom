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

import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterIT extends WisdomBlackBoxTest {

    @Test
    public void testIntegerParameterFromPath() throws Exception {
        HttpResponse<String> response = get("/parameter/integer/10").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("10");
    }

    @Test
    public void testLongParameterFromPath() throws Exception {
        HttpResponse<String> response = get("/parameter/long/11").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("11");
    }

    @Test
    public void testStringParameterFromPath() throws Exception {
        HttpResponse<String> response = get("/parameter/string/hello").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("hello");
    }

    @Test
    public void testBooleanParameterFromPath() throws Exception {
        HttpResponse<String> response = get("/parameter/boolean/true").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/boolean/false").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("false");

        response = get("/parameter/boolean/1").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/boolean/on").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/boolean/yes").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/boolean/off").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("false");

    }

    @Test
    public void testIntegerParameterFromQuery() throws Exception {
        HttpResponse<String> response = get("/parameter/query/integer?i=10").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("10");
    }

    @Test
    public void testLongParameterFromQuery() throws Exception {
        HttpResponse<String> response = get("/parameter/query/long?l=11").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("11");
    }

    @Test
    public void testStringParameterFromQuery() throws Exception {
        HttpResponse<String> response = get("/parameter/query/string?s=hello").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("hello");
    }

    @Test
    public void testArrayParameterFromQuery() throws Exception {
        HttpResponse<String> response = get("/parameter/query/array?x=1&x=2&x=3").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("123");

        response = get("/parameter/query/array").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("");

        response = get("/parameter/query/array?").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("");
    }

    @Test
    public void testListParameterFromQuery() throws Exception {
        HttpResponse<String> response = get("/parameter/query/list?x=1&x=2&x=3").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("123");

        response = get("/parameter/query/list").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("");

        response = get("/parameter/query/list?").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("");
    }

    @Test
    public void testBooleanParameterFromQuery() throws Exception {
        HttpResponse<String> response = get("/parameter/query/boolean?b=true").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/query/boolean?b=false").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("false");

        response = get("/parameter/query/boolean?b=on").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/query/boolean?b=yes").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/query/boolean?b=1").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        response = get("/parameter/query/boolean?b=0").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("false");
    }

    @Test
    public void testBooleanParameterFromQueryWithDefault() throws Exception {
        HttpResponse<String> response = get("/parameter/query/boolean/default?b=true").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");

        // Use default value
        response = get("/parameter/query/boolean/default").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("true");
    }

}
