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
package external;

import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the Pegdown controller works.
 */
public class PegdownControllerIT extends WisdomBlackBoxTest {

    @Test
    public void testPegdown() throws Exception {
        HttpResponse<String> response = post("/pegdown").body(
                "# Hello Wisdom\n" +
                        "\n" +
                        "* Hi\n" +
                        "* This\n" +
                        "* Is\n" +
                        "* Cool").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body())
                .contains("<h1>Hello Wisdom</h1>")
                .contains("<li>This</li>")
                .contains("<li>Is</li>")
                .contains("<li>Cool</li>")
                .contains("</ul>");
    }

}
