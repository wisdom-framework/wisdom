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

/**
 * Check error handling.
 */
public class ErrorHandlingIT extends WisdomBlackBoxTest {

    /**
     * Check internal error generation.
     */
    @Test
    public void testWithException() throws Exception {
        HttpResponse<String> response = get("/error/500").asString();
        assertThat(response.code()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testWithHttpException() throws Exception {
        HttpResponse<String> response = get("/error/418").asString();
        assertThat(response.code()).isEqualTo(418);
        assertThat(response.body()).isEqualTo("bad");
    }

    @Test
    public void testWithMappedException() throws Exception {
        HttpResponse<String> response = get("/error/300").asString();
        assertThat(response.code()).isEqualTo(300);
        assertThat(response.body()).isEqualTo("nobody");
    }
}
