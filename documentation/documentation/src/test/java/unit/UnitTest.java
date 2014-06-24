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
// tag::unit[]

import snippets.controllers.Name;
import snippets.controllers.Simple;
import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomUnitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

public class UnitTest extends WisdomUnitTest {

    @Test
    public void test() {
        assertThat(1 + 1).isEqualTo(2);
    }

    @Test
    public void testActionWithoutParameter() {
        // This is an instance of my controller
        final Simple simple = new Simple();

        // Call the action method as follows:
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return simple.index();
            }
        }).parameter("name", "clement").header("Accept", "text/html").invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("Follow the path to Wisdom");
    }

    @Test
    public void testActionWithParameter() {
        // This is an instance of my controller
        final Name name = new Name();

        // Call the action method as follows:
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return name.index("clement");
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("Hi " + "clement" + ", follow us on the Wisdom path");
    }
}
// end::unit[]
