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
package sample;

import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * An in-container test checking your application while it's executing.
 */
public class InContainerIT extends WisdomTest {

    /**
     * First inject your controller. The @Inject annotation is able to
     * inject (in tests) the bundle context, controllers, services and
     * templates.
     */
    @Inject
    WelcomeController controller;

    @Test
    public void testWelcomePage() {
        // Wrap your controller invocation so you can configure the HTTP
        // Context (parameter, header...)
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.welcome();
            }
        }).header("foo", "bar").invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).contains("Wisdom");
    }
}
