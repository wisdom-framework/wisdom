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
 * Checks validation.
 */
public class ValidationIT extends WisdomBlackBoxTest {

    @Test
    public void testAutomaticValidationOfParameter() throws Exception {
        HttpResponse<String> response = get("/validation/auto?email=wisdom@wisdom-framework.org").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo("wisdom@wisdom-framework.org");

        response = get("/validation/auto").asString();
        assertThat(response.code()).isEqualTo(BAD_REQUEST);
        // Use 'nul' to support 'any' locale
        assertThat(response.body()).contains("nul");

        response = get("/validation/auto?email=this_is_not_an_email_address").asString();
        assertThat(response.code()).isEqualTo(BAD_REQUEST);
        assertThat(response.body()).contains("invalid").contains("email");
    }
}
