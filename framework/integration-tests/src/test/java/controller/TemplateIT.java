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

public class TemplateIT extends WisdomBlackBoxTest {

    @Test
    public void testEscaping() throws Exception {
        String expected = "&lt;script&gt;alert(&#39;Hello! &lt;&gt;&amp;&quot;&#39;&#39;);&lt;/script&gt;";
        HttpResponse<String> response = get("/templates/escaping").asString();
        assertThat(response.body()).contains(expected);
    }

    @Test
    public void testRequestScope() throws Exception {
        String expected = "<h2>2</h2>";
        HttpResponse<String> response = get("/templates/scope").asString();
        assertThat(response.body()).contains(expected);
    }

    @Test
    public void testStaticMethodAccess() throws Exception {
        HttpResponse<String> response = get("/templates/static").asString();
        assertThat(response.body())
                .contains("<p>3</p>")
                .contains("GSSManagerImpl@");
    }

}
