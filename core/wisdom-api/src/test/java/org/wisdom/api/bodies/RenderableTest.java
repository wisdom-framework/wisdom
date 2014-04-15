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
package org.wisdom.api.bodies;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the Empty Http Body
 */
public class RenderableTest {


    @Test
    public void testNoHttpBody() throws Exception {
        NoHttpBody body = new NoHttpBody();
        assertThat(body.length()).isEqualTo(0);
        assertThat(body.content()).isNull();
        assertThat(body.mimetype()).isEqualTo(MimeTypes.TEXT);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(bytes).isEmpty();
    }

    @Test
    public void testRenderableString() throws Exception {
        final String hello = "hello";
        RenderableString body = new RenderableString(hello);
        assertThat(body.length()).isEqualTo(hello.length());
        assertThat(body.content()).isEqualTo(hello);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.HTML);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(hello);

        body = new RenderableString(hello, MimeTypes.TEXT);
        assertThat(body.length()).isEqualTo(hello.length());
        assertThat(body.content()).isEqualTo(hello);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.TEXT);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(hello);

        body = new RenderableString(new StringBuilder().append("hello"));
        assertThat(body.length()).isEqualTo(hello.length());
        assertThat(body.content()).isEqualTo(hello);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.HTML);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(hello);

        body = new RenderableString(new StringBuffer().append("hello"));
        assertThat(body.length()).isEqualTo(hello.length());
        assertThat(body.content()).isEqualTo(hello);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.HTML);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(hello);

        body = new RenderableString(new StringBuffer().append("hello"), MimeTypes.TEXT);
        assertThat(body.length()).isEqualTo(hello.length());
        assertThat(body.content()).isEqualTo(hello);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.TEXT);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(hello);

        body = new RenderableString(new StringWriter().append("hello"));
        assertThat(body.length()).isEqualTo(hello.length());
        assertThat(body.content()).isEqualTo(hello);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.HTML);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(hello);

        body = new RenderableString(new StringWriter().append("hello"), MimeTypes.JAVASCRIPT);
        assertThat(body.length()).isEqualTo(hello.length());
        assertThat(body.content()).isEqualTo(hello);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.JAVASCRIPT);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(hello);
    }

    @Test
    public void testRenderableFile() throws Exception {
        final File file = new File("target/test-classes/a_file.txt");
        RenderableFile body = new RenderableFile(file);
        assertThat(body.length()).isEqualTo(file.length());
        assertThat(body.content()).isEqualTo(file);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.TEXT);
        assertThat(body.mustBeChunked()).isTrue();
        assertThat(body.requireSerializer()).isFalse();
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo("Used as test data.");
    }

    @Test
    public void testRenderableFileAsChunked() throws Exception {
        final File file = new File("target/test-classes/a_file.txt");
        RenderableFile body = new RenderableFile(file, false);
        assertThat(body.length()).isEqualTo(file.length());
        assertThat(body.content()).isEqualTo(file);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.TEXT);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo("Used as test data.");
    }

    @Test
    public void testRenderableStream() throws Exception {
        final File file = new File("target/test-classes/a_file.txt");
        RenderableStream body = new RenderableStream(FileUtils.openInputStream(file));
        // Unknown size and mime type
        assertThat(body.length()).isEqualTo(-1);
        assertThat(body.mimetype()).isNull();
        assertThat(body.mustBeChunked()).isTrue();
        assertThat(body.requireSerializer()).isFalse();
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo("Used as test data.");
    }

    @Test
    public void testRenderableUrl() throws Exception {
        final File file = new File("target/test-classes/a_file.txt");
        RenderableURL body = new RenderableURL(file.toURI().toURL());
        // Unknown size but mime type determined
        assertThat(body.length()).isEqualTo(-1);
        assertThat(body.mimetype()).isEqualTo(MimeTypes.TEXT);
        assertThat(body.mustBeChunked()).isTrue();
        assertThat(body.requireSerializer()).isFalse();
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo("Used as test data.");
    }

    @Test
    public void testRenderableJson() throws Exception {
        String LF = System.getProperty("line.separator");
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("message", "hello");
        String nodeasString = "{" + LF +
                "  \"message\" : \"hello\"" + LF +
                "}";
        RenderableJson body = new RenderableJson(node);
        assertThat(body.length()).isEqualTo(nodeasString.length());
        assertThat(body.mimetype()).isEqualTo(MimeTypes.JSON);
        assertThat(body.content()).isEqualTo(node);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isFalse();
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(nodeasString);
    }

    @Test
    public void testRenderableObject() throws Exception {
        List<String> list = Arrays.asList("a", "b", "c");
        RenderableObject body = new RenderableObject(list);
        // Unknown
        assertThat(body.length()).isEqualTo(-1);
        assertThat(body.mimetype()).isEqualTo(null);

        assertThat(body.content()).isEqualTo(list);
        assertThat(body.mustBeChunked()).isFalse();
        assertThat(body.requireSerializer()).isTrue();
        body.setSerializedForm("a,b,c");
        byte[] bytes = IOUtils.toByteArray(body.render(null, null));
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo("a,b,c");
    }

}
