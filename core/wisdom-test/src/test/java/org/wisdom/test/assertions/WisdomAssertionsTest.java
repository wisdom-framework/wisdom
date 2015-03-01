/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.test.assertions;

import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;

import static org.wisdom.test.assertions.WisdomAssertions.assertStatus;
import static org.wisdom.test.assertions.WisdomAssertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * Checks that we can retrieve our assertions form a single class
 */
public class WisdomAssertionsTest {

    @Test
    public void testAssertThat() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok();
            }
        }).with().parameter("foo", "bar").invoke();

        assertThat(result).isNotNull();
        assertThat(result).hasStatus(200);
        assertThat(result.getContext()).hasParameter("foo", "bar");
        assertThat(result.getContext().session()).isEmpty();
        assertStatus(result.getResult().getStatusCode()).isOk();
    }

}