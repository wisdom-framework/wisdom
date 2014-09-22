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

public class LogIT extends WisdomBlackBoxTest {

    @Test
    public void testSLF4J() throws Exception {
        String message = "logging with slf4j @ " + System.currentTimeMillis();
        HttpResponse<String> response = get("/log/slf4j?message=" + message).asString();
        assertThat(response.code()).isEqualTo(OK);

        response = get("/log").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains(message);
    }

    @Test
    public void testJUL() throws Exception {
        String message = "logging with jul @ " + System.currentTimeMillis();
        HttpResponse<String> response = get("/log/jul?message=" + message).asString();
        assertThat(response.code()).isEqualTo(OK);

        response = get("/log").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains(message);
    }

    @Test
    public void testJCL() throws Exception {
        String message = "logging with JCL @ " + System.currentTimeMillis();
        HttpResponse<String> response = get("/log/jcl?message=" + message).asString();
        assertThat(response.code()).isEqualTo(OK);

        response = get("/log").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains(message);
    }

    @Test
    public void testLog4J() throws Exception {
        String message = "logging with log4j @ " + System.currentTimeMillis();
        HttpResponse<String> response = get("/log/log4j?message=" + message).asString();
        assertThat(response.code()).isEqualTo(OK);

        response = get("/log").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains(message);
    }
}
