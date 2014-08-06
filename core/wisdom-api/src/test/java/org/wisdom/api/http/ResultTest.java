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
package org.wisdom.api.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.wisdom.api.bodies.NoHttpBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.api.cookies.Cookie.cookie;

/**
 * Checks the Result class.
 */
public class ResultTest {
    @Test
    public void testRenderWithStrings() throws Exception {
        Result result = Results.ok().render("hello");
        assertThat(result.getRenderable()).isNotNull();
        assertThat(result.getRenderable().content()).isEqualTo("hello");
        assertThat(result.getRenderable().requireSerializer()).isFalse();
        assertThat(result.getRenderable().length()).isGreaterThan(0);
        assertThat(result.getRenderable().mustBeChunked()).isFalse();

        result = Results.ok().render(new StringBuilder("hello"));
        assertThat(result.getRenderable()).isNotNull();
        assertThat(result.getRenderable().content()).isEqualTo("hello");
        assertThat(result.getRenderable().requireSerializer()).isFalse();
        assertThat(result.getRenderable().length()).isGreaterThan(0);
        assertThat(result.getRenderable().mustBeChunked()).isFalse();

        result = Results.ok().render(new StringBuffer("hello"));
        assertThat(result.getRenderable()).isNotNull();
        assertThat(result.getRenderable().content()).isEqualTo("hello");
        assertThat(result.getRenderable().requireSerializer()).isFalse();
        assertThat(result.getRenderable().length()).isGreaterThan(0);
        assertThat(result.getRenderable().mustBeChunked()).isFalse();
    }

    @Test
    public void testRenderObjects() throws Exception {
        Result result = Results.ok().render(ImmutableList.of());
        assertThat(result.getRenderable().requireSerializer()).isTrue();
        assertThat(result.getRenderable().length()).isLessThan(0);
    }

    @Test
    public void testRenderJson() throws Exception {
        Result result = Results.ok().render(new ObjectMapper().createObjectNode().put("hello", "value"));
        assertThat(result.getContentType()).isEqualTo(MimeTypes.JSON);
        assertThat(result.getRenderable().requireSerializer()).isFalse();
        assertThat(result.getRenderable().length()).isGreaterThan(0);
        assertThat(result.getRenderable().mustBeChunked()).isFalse();
    }

    @Test
    public void testGetContentType() throws Exception {
        Result result = Results.ok().json();
        assertThat(result.getContentType()).contains(MimeTypes.JSON).doesNotContain(Charsets.UTF_8.toString());
        result = Results.ok().as(MimeTypes.BINARY);
        assertThat(result.getContentType()).contains(MimeTypes.BINARY).doesNotContain(Charsets.UTF_8.toString());
    }

    @Test
    public void testGetFullContentType() throws Exception {
        Result result = Results.ok().json();
        assertThat(result.getFullContentType()).contains(MimeTypes.JSON).contains(Charsets.UTF_8.toString());
        result = Results.ok().as(MimeTypes.BINARY);
        assertThat(result.getFullContentType()).contains(MimeTypes.BINARY).doesNotContain(Charsets.UTF_8.toString());
    }

    @Test
    public void testAs() throws Exception {
        Result result = new Result(200).as(MimeTypes.CSS);
        assertThat(result.getContentType()).isEqualTo(MimeTypes.CSS);
        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualTo(MimeTypes.CSS);
        assertThat(result.getCharset()).isNotEqualTo(Charsets.UTF_8);

        // Check complete mime type
        result = new Result(200).as("application/json; charset=utf-8").render("{}");
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);
        assertThat(result.getContentType()).contains(MimeTypes.JSON);
        result.getRenderable().render(null, result);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);
    }

    @Test
    public void testGetHeaders() throws Exception {
        Result result = Results.ok();
        assertThat(result.getHeaders()).isEmpty();
        result.with("Header", "Value");
        assertThat(result.getHeaders().get("Header")).isEqualTo("Value");
    }

    @Test
    public void testWithCharset() throws Exception {
        Result result = Results.ok().with(Charsets.US_ASCII);
        assertThat(result.getCharset()).isEqualTo(Charsets.US_ASCII);
    }

    @Test
    public void testGetCookies() throws Exception {
        Result result = Results.ok();
        assertThat(result.getCookies()).isEmpty();

        result.with(cookie("hello", "value").build());
        assertThat(result.getCookies()).hasSize(1);
        assertThat(result.getCookie("hello").value()).isEqualTo("value");
        assertThat(result.getCookie("missing")).isNull();
    }

    @Test
    public void testWithout() throws Exception {
        Result result = Results.ok()
                .with("header", "value")
                .with(cookie("hello", "value").setMaxAge(10L).build());

        assertThat(result.getHeaders().get("header")).isEqualTo("value");
        result.without("header");
        assertThat(result.getHeaders().get("header")).isNull();
        result.without("hello");
        assertThat(result.getCookie("hello").maxAge()).isEqualTo(0L);
    }

    @Test
    public void testDiscard() throws Exception {
        Result result = Results.ok().discard("cookie");
        // A cookie is discarded by setting its max-age to 0
        assertThat(result.getCookie("cookie").maxAge()).isEqualTo(0L);

        result.with(cookie("hello", "value").setMaxAge(10L).build());
        result.discard("hello");
        assertThat(result.getCookie("hello").maxAge()).isEqualTo(0L);
    }

    @Test
    public void testStatus() throws Exception {
        Result result = Results.ok().status(418);
        // I'm a teapot
        assertThat(result.getStatusCode()).isEqualTo(418);

    }

    @Test
    public void testRedirect() throws Exception {
        Result result = new Result(200).redirect("/foo");
        assertThat(result.getStatusCode()).isEqualTo(Status.SEE_OTHER);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("/foo");

    }

    @Test
    public void testRedirectTemporary() throws Exception {
        Result result = new Result(200).redirectTemporary("/foo");
        assertThat(result.getStatusCode()).isEqualTo(Status.TEMPORARY_REDIRECT);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("/foo");
    }

    @Test
    public void testHtml() throws Exception {
        Result result = new Result(200).html();
        assertThat(result.getContentType()).isEqualTo(MimeTypes.HTML);
        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualTo(MimeTypes.HTML);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);
    }

    @Test
    public void testJson() throws Exception {
        Result result = new Result(200).json();
        assertThat(result.getContentType()).isEqualTo(MimeTypes.JSON);
        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualTo(MimeTypes.JSON);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);

    }

    @Test
    public void testXml() throws Exception {
        Result result = new Result(200).xml();
        assertThat(result.getContentType()).isEqualTo(MimeTypes.XML);
        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualTo(MimeTypes.XML);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);
    }

    @Test
    public void testNoCache() throws Exception {
        Result result = new Result(200);
        result.noCache();
        assertThat(result.getHeaders()).containsEntry(HeaderNames.CACHE_CONTROL, HeaderNames.NOCACHE_VALUE);
    }

    @Test
    public void testNoContentIfNone() throws Exception {
        Result result = new Result(200);
        // We don't have content.
        assertThat(result.getRenderable()).isNull();
        result.noContentIfNone();
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);

        result = Results.ok("hello");
        result.noContentIfNone();
        assertThat(result.getRenderable()).isNotInstanceOf(NoHttpBody.class);
    }
}
