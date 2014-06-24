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
package router;


import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlDecodingIT extends WisdomBlackBoxTest {

    @Test
    public void testURLDecoding() throws Exception {
        testDecoding("a", "a", "a", "a", "a", "a");
        testDecoding("%2B", "%2B", "%2B", "+", "%2B", "+");
        testDecoding("+", "+", "+", "+", "+", " ");
        testDecoding("%20", "%20", "%20", " ", "%20", " ");
        testDecoding("&", "&", "-", "&", "&", "-");
        testDecoding("=", "=", "-", "=", "=", "-");
    }

    public void testDecoding(String p1, String p2, String q, String expected1, String expected2,
                             String expected3) throws Exception {
        final String url = "/urlcoding/" + p1 + "/" + p2 + "?q=" + q;
        HttpResponse<String> response = get(url).asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).isEqualTo(expected1 + "," + expected2 + "," + expected3);
    }

}
