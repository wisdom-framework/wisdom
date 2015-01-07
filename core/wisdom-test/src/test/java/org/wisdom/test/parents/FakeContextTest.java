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

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.router.Route;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the Fake Context development model and behavior.
 */
public class FakeContextTest {
    @Test
    public void testId() throws Exception {
        FakeContext context1 = new FakeContext();
        FakeContext context2 = new FakeContext();
        assertThat(context2.id()).isGreaterThan(context1.id());
    }

    @Test
    public void testPath() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.path()).isNull();
        context.setPath("/foo");
        assertThat(context.path()).isEqualTo("/foo");
    }

    @Test
    public void testFlash() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.flash().getCurrentFlashCookieData()).isEmpty();
        assertThat(context.flash().getOutgoingFlashCookieData()).isEmpty();

        context.flash().put("k", "v");
        assertThat(context.flash().get("k")).isEqualTo("v");
    }

    @Test
    public void testSession() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.session().getData()).isEmpty();

        context.session().put("k", "v");
        assertThat(context.session().get("k")).isEqualTo("v");
    }

    @Test
    public void testCookie() throws Exception {
        FakeContext context = new FakeContext()
                .setCookie("hello", "wisdom")
                .setCookie(Cookie.cookie("test", "value").setComment("comment").build());
        assertThat(context.cookie("hello").value()).isEqualTo("wisdom");
        assertThat(context.cookie("test").value()).isEqualTo("value");
        assertThat(context.cookie("test").comment()).isEqualTo("comment");
        assertThat(context.cookie("none")).isNull();
        assertThat(context.cookies().get("test")).isNotNull();
        assertThat(context.cookies().get("none")).isNull();
        assertThat(context.cookieValue("hello")).isEqualTo("wisdom");
        assertThat(context.cookieValue("none")).isNull();

        assertThat(context.hasCookie("hello")).isTrue();
        assertThat(context.hasCookie("none")).isFalse();

        context = new FakeContext();
        assertThat(context.hasCookie("hello")).isFalse();
        assertThat(context.hasCookie("none")).isFalse();
        assertThat(context.cookies().get("none")).isNull();
    }

    @Test
    public void testGetContextPath() throws Exception {
        assertThat(new FakeContext().contextPath()).isNull();
    }

    @Test
    public void testParameter() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.parameter("none")).isNull();
        assertThat(context.parameter("none", "hello")).isEqualTo("hello");
        assertThat(context.parameterMultipleValues("none")).isEmpty();

        context
                .setParameter("hello", "wisdom")
                .setParameter("multiple", ImmutableList.of("a", "b"));

        assertThat(context.parameter("hello")).isEqualTo("wisdom");
        assertThat(context.parameter("hello", "nope")).isEqualTo("wisdom");
        assertThat(context.parameter("multiple")).isEqualTo("a");
        assertThat(context.parameterMultipleValues("multiple")).containsExactly("a", "b");

        assertThat(context.parameters()).hasSize(2);
    }

    @Test
    public void testParameterAsInteger() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.parameterAsInteger("none")).isNull();
        assertThat(context.parameterAsInteger("none", 1)).isEqualTo(1);

        context
                .setParameter("hello", "4");

        assertThat(context.parameter("hello")).isEqualTo("4");
        assertThat(context.parameterAsInteger("hello")).isEqualTo(4);
        assertThat(context.parameterAsInteger("hello", 1)).isEqualTo(4);
    }

    @Test
    public void testParameterAsBoolean() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.parameterAsBoolean("none")).isNull();
        assertThat(context.parameterAsBoolean("none", true)).isTrue();

        context
                .setParameter("hello", "true");

        assertThat(context.parameterAsBoolean("hello")).isTrue();
        assertThat(context.parameterAsBoolean("hello", false)).isTrue();

        assertThat(context.parameters()).hasSize(1);
    }

    @Test
    public void testParameterFromPath() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.parameterFromPath("hello")).isNull();
        assertThat(context.parameterFromPathAsInteger("hello")).isNull();
        assertThat(context.parameterFromPathEncoded("hello")).isNull();

        context.setParameter("hello", "wisdom")
                .setParameter("int", "1");

        assertThat(context.parameterFromPath("hello")).isEqualTo("wisdom");
        assertThat(context.parameterFromPathAsInteger("int")).isEqualTo(1);
        assertThat(context.parameterFromPathEncoded("hello")).isEqualTo("wisdom");
    }

    @Test
    public void testHeader() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.headers()).isEmpty();

        context.setHeader("h1", "v1").setHeader("h2", "a", "b");

        assertThat(context.headers()).hasSize(2);
        assertThat(context.header("h1")).isEqualTo("v1");
        assertThat(context.header("h2")).isEqualTo("a");
        assertThat(context.headers("h2")).containsExactly("a", "b");

        assertThat(context.header("none")).isNull();
        assertThat(context.headers("none")).isEmpty();
    }

    @Test
    public void testBody() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.body()).isNull();
        assertThat(context.body(String.class)).isNull();
        assertThat(context.reader()).isNull();

        final String content = "<h1>Hello</h1>";
        context.setBody(content);
        assertThat(context.body()).isEqualTo(content);
        assertThat(context.body(String.class)).isEqualTo(content);
        assertThat(IOUtils.toString(context.reader())).isEqualTo(content);
    }

    @Test
    public void testGetRoute() throws Exception {
        // route are not set by fake implementations
        FakeContext context = new FakeContext();
        final Route route = new Route(HttpMethod.GET, "/", null, null);
        context.route(route);
        assertThat(context.route()).isEqualTo(route);
    }


    @Test
    public void testFormData() throws Exception {
        FakeContext context = new FakeContext();
        assertThat(context.form()).isEmpty();
        assertThat(context.files()).isEmpty();
        assertThat(context.isMultipart()).isFalse();


        context.setFormField("name", "wisdom")
                .setFormField("age", "1")
                .setFormField("file", new File("src/test/resources/foo.txt"));

        assertThat(context.form().get("name")).contains("wisdom");
        assertThat(context.form().get("age")).contains("1");

        assertThat(context.files()).hasSize(1);
        assertThat(context.file("file").name()).isEqualTo("foo.txt");
        assertThat(context.file("file").field()).isEqualTo("file");

        assertThat(context.isMultipart()).isTrue();
    }

    @Test
    public void testRequest() throws Exception {
        FakeContext context = new FakeContext()
                .setParameter("hello", "wisdom")
                .setParameter("int", "2")
                .setParameter("bool", "true")
                .setCookie("cook", "ie")
                .setPath("/foo")
                .setHeader(HeaderNames.CONTENT_TYPE, "plain/text");

        assertThat(context.request().contentType()).isEqualTo("plain/text");

        assertThat(context.request().parameter("hello")).isEqualTo("wisdom");
        assertThat(context.request().parameter("hello", "2")).isEqualTo("wisdom");
        assertThat(context.request().parameter("hello2", "2")).isEqualTo("2");
        assertThat(context.request().parameterMultipleValues("hello")).contains("wisdom");

        assertThat(context.request().parameterAsInteger("int")).isEqualTo(2);
        assertThat(context.request().parameterAsInteger("int", 4)).isEqualTo(2);
        assertThat(context.request().parameterAsInteger("int2", 4)).isEqualTo(4);

        assertThat(context.request().parameterAsBoolean("bool")).isTrue();
        assertThat(context.request().parameterAsBoolean("bool", false)).isTrue();
        assertThat(context.request().parameterAsBoolean("bool2", true)).isTrue();

        assertThat(context.request().headers()).hasSize(1);
        assertThat(context.request().getHeader(HeaderNames.CONTENT_TYPE)).isEqualTo("plain/text");

        assertThat(context.request().path()).isEqualTo("/foo");

        assertThat(context.request().cookie("cook").value()).isEqualTo("ie");
        assertThat(context.request().cookie("none")).isNull();
        assertThat(context.request().cookies().get("none")).isNull();
        assertThat(context.request().cookies().get("cook").value()).isEqualTo("ie");

        //Check null values.
        assertThat(context.request().method()).isNull();
        assertThat(context.request().remoteAddress()).isNull();
        assertThat(context.request().host()).isNull();
        assertThat(context.request().username()).isNull();
        assertThat(context.request().charset()).isNull();
        assertThat(context.request().mediaType().toString()).isEqualTo("text/*");
        assertThat(context.request().mediaTypes()).hasSize(1);
        assertThat(context.request().language()).isNull();
        assertThat(context.request().languages()).isEmpty();
    }
}
