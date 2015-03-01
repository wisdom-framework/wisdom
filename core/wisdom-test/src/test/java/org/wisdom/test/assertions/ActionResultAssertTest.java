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

import com.google.common.base.Charsets;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;

import static org.junit.Assert.*;
import static org.wisdom.test.parents.Action.action;

/**
 * Checks the {@link ActionResultAssert}.
 */
public class ActionResultAssertTest {

    @Test
    public void testStatus() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok();
            }
        }).invoke();
        ActionResultAssert.assertThat(result)
                .isNotNull()
                .status().isOk();

        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.badRequest();
            }
        }).invoke();
        ActionResultAssert.assertThat(result).status().isBadRequest();

    }

    @Test
    public void testHasStatus() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok();
            }
        }).invoke();
        ActionResultAssert.assertThat(result).hasStatus(200);

        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.badRequest();
            }
        }).invoke();
        ActionResultAssert.assertThat(result).hasStatus(400);

    }

    @Test
    public void testHasContentType() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok().json();
            }
        }).invoke();
        ActionResultAssert.assertThat(result)
                .hasContentType(MimeTypes.JSON)
                .hasFullContentType(MimeTypes.JSON + "; charset=UTF-8")
                .hasCharset(Charsets.UTF_8);
    }

    @Test
    public void testHasInSession() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok().json().addToSession("foo", "bar");
            }
        }).invoke();

        ActionResultAssert.assertThat(result)
                .sessionIsNotEmpty()
                .hasInSession("foo", "bar")
                .doesNotHaveInSession("xxx");
    }

    @Test
    public void testSessionIsEmpty() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok().json();
            }
        }).invoke();

        ActionResultAssert.assertThat(result)
                .sessionIsEmpty();
    }

    private class Form {
        public final String name;

        private Form(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Form form = (Form) o;
            return name.equals(form.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    @Test
    public void testHasBody() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok(new Form("wisdom")).json();
            }
        }).invoke();

        ActionResultAssert.assertThat(result)
                .hasContent(Form.class, new Form("wisdom"));
    }

    @Test
    public void testHasBodyWithString() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok("wisdom").json();
            }
        }).invoke();

        ActionResultAssert.assertThat(result)
                .hasContent("wisdom")
                .hasInContent("wis")
                .hasInContent("dom")
                .hasContentMatch("wis(.*)");
    }

    @Test
    public void testWhenActionResultIsNull() {
        try {
            ActionResultAssert.assertThat(null);
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }

        // null result
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return null;
            }
        }).invoke();

        try {
            ActionResultAssert.assertThat(result);
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }
    }

    @Test
    public void testHeaderCheckErrors() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok("wisdom").json();
            }
        }).invoke();


        try {
            ActionResultAssert.assertThat(result)
                    .hasStatus(400);
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }

        try {
            ActionResultAssert.assertThat(result)
                    .hasCharset(Charsets.ISO_8859_1);
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }

        try {
            ActionResultAssert.assertThat(result)
                    .hasContentType(MimeTypes.HTML);
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }

        try {
            ActionResultAssert.assertThat(result)
                    .hasFullContentType("nope");
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }
    }

    @Test
    public void testBodyCheckErrors() throws Exception {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return Results.ok("wisdom").json();
            }
        }).invoke();


       try {
           ActionResultAssert.assertThat(result)
                   .hasContent("should not be");
           fail("Assertion Error expected");
       } catch (AssertionError error) {
           // Expected
       }

        try {
            ActionResultAssert.assertThat(result)
                    .hasInContent("should not be");
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }

        try {
            ActionResultAssert.assertThat(result)
                    .hasContentMatch("should not be");
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }

        try {
            ActionResultAssert.assertThat(result)
                    .hasContent(Form.class, new Form("hello"));
            fail("Assertion Error expected");
        } catch (AssertionError error) {
            // Expected
        }
    }
}