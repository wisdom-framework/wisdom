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
package org.wisdom.test.parents;

import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the action syntax.
 */
public class ActionTest {


    @Test
    public void ok() {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok();
            }
        }).invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
    }

    @Test
    public void error() {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                throw new NullPointerException();
            }
        }).invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(500);
    }

    @Test
    public void testParameters() {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                if (context().parameter("p").equals("v")) {
                    return Results.ok();
                } else {
                    return Results.badRequest();
                }
            }
        }).parameter("p", "v").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testHeaders() {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                if (context().header("p").equals("v")) {
                    return Results.ok();
                } else {
                    return Results.badRequest();
                }
            }
        }).header("p", "v").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testForm() {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                if (context().form().get("p").get(0).equals("v")) {
                    return Results.ok();
                } else {
                    return Results.badRequest();
                }
            }
        }).attribute("p", "v").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testBody() {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                if (context().body().contains("Hello")) {
                    return Results.ok();
                } else {
                    return Results.badRequest();
                }
            }
        }).body("<h1>Hello</h1>").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testContext() {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                if (context().parameter("p").equals("v")
                        && context().cookieValue("c").equals("cc")) {
                    return Results.ok();
                } else {
                    return Results.badRequest();
                }
            }
        }).with(new FakeContext().setParameter("p", "v").setCookie("c", "cc")).invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
    }
}
