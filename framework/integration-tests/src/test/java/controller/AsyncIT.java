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
import org.wisdom.api.http.Status;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncIT extends WisdomBlackBoxTest {

    @Test
    public void testSimpleAsync() throws Exception {
        Future<HttpResponse<String>> ar = get("/hello/async/simple").asStringAsync();
        HttpResponse<String> response = ar.get(1, TimeUnit.MINUTES);
        assertThat(response.body()).isEqualTo("x");
        assertThat(response.code()).isEqualTo(OK);
    }

    @Test
    public void testAsyncWithAnnotation() throws Exception {
        Future<HttpResponse<String>> ar = get("/hello/async/annotation").asStringAsync();
        HttpResponse<String> response = ar.get(1, TimeUnit.MINUTES);
        assertThat(response.body()).isEqualTo("x");
        assertThat(response.code()).isEqualTo(OK);
    }

    @Test
    public void testAsyncWithCompleteAnnotation() throws Exception {
        Future<HttpResponse<String>> ar = get("/hello/async/complete_annotation").asStringAsync();
        HttpResponse<String> response = ar.get(1, TimeUnit.MINUTES);
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("x");
    }

    @Test
    public void testAsyncTimeout() throws Exception {
        Future<HttpResponse<String>> ar = get("/hello/async/timeout").asStringAsync();
        HttpResponse<String> response = ar.get(1, TimeUnit.MINUTES);
        assertThat(response.body()).containsIgnoringCase("Request Timeout");
        assertThat(response.code()).isEqualTo(Status.GATEWAY_TIMEOUT);
    }

    @Test
    public void testAsyncCompleteTimeout() throws Exception {
        Future<HttpResponse<String>> ar = get("/hello/async/complete_timeout").asStringAsync();
        HttpResponse<String> response = ar.get(1, TimeUnit.MINUTES);
        assertThat(response.code()).isEqualTo(Status.GATEWAY_TIMEOUT);
        assertThat(response.body()).containsIgnoringCase("Request Timeout");
    }
}
