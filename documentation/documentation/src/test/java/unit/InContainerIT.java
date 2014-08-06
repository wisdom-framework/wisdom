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
package unit;
// tag::IT[]

import controllers.Documentation;
import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

public class InContainerIT extends WisdomTest {

    // Inject the controllers, services or template you are testing

    @Inject
    Documentation documentation;

    @Test
    public void testDocumentation() {
        // Call the action method as follows:
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return documentation.doc();
            }
        }).header("Accept", "text/html").invoke();

        // It returns a redirection to the index.html page.
        assertThat(status(result)).isEqualTo(SEE_OTHER);
        assertThat(result.getResult().getHeaders().get(LOCATION)).contains("/index.html");
    }

    @Test
    public void test() {
        // Not recommended, but this is also executed
        assertThat(1 + 1).isEqualTo(2);
    }

}
// end::IT[]
