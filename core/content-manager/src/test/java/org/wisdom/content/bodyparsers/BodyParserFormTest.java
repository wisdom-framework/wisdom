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
package org.wisdom.content.bodyparsers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.DefaultController;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.content.converters.ParamConverterEngine;
import org.wisdom.test.parents.FakeContext;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class BodyParserFormTest {

    BodyParserForm parser = new BodyParserForm();

    // Create a valid route to get path parameters.
    Route route = new RouteBuilder().route(HttpMethod.POST)
            .on("/post/{path}")
            .to(new DefaultController() {
                public Result index() {
                    return ok();
                }
            }, "index");

    @Before
    public void setUp() {
        parser.converters = new ParamConverterEngine(Collections.<ParameterConverter>emptyList(),
                Collections.<ParameterFactory>emptyList());
    }

    @Test
    public void testSupportedTypes() {
        assertThat(parser.getContentTypes()).containsExactly(MimeTypes.FORM, MimeTypes.MULTIPART);
    }

    @Test
    public void testThatInstancesCanBeCreatedFromParameters() {
        FakeContext context = new FakeContext()
                .setParameter("name", "wisdom")
                .setParameter("id", "0")
                .setParameter("time", "1000")
                .setParameter("flavors", ImmutableList.of("a", "b"))
                .setParameter("not_bound", "not_bound");
        context.route(route);
        context.getFakeRequest().uri("/post/2");

        Body body = parser.invoke(context, Body.class);
        assertThat(body.name).isEqualToIgnoringCase("wisdom");
        assertThat(body.id).isEqualTo(0);
        assertThat(body.time).isEqualTo(1000l);
        assertThat(body.flavors).containsExactly("a", "b");
        assertThat(body.path).isEqualTo(2);
    }

    @Test
    public void testThatInstancesCanBeCreatedWithSetters() {
        FakeContext context = new FakeContext()
                .setParameter("foo", "wisdom")
                .setParameter("id", "0")
                .setParameter("notused", "notused")
                .setParameter("time", "1000")
                .setParameter("flavors", ImmutableList.of("a", "b"))
                .setParameter("not_bound", "not_bound");

        context.route(route);
        context.getFakeRequest().uri("/post/2");

        BodyWithSetter body = parser.invoke(context, BodyWithSetter.class);
        assertThat(body.name).isEqualToIgnoringCase("wisdom-set");
        assertThat(body.id).isEqualTo(0);
        assertThat(body.time).isEqualTo(1000l);
        assertThat(body.flavors).containsExactly("a", "b");
        assertThat(body.path).isEqualTo(2);
    }

    @Test
    public void testThatInstancesCanBeCreatedFromFormParameters() {
        FakeContext context = new FakeContext()
                .setFormField("name", "wisdom")
                .setFormField("id", "0")
                .setFormField("time", "1000")
                .setFormField("flavors", "a", "b")
                .setFormField("not_bound", "not_bound");
        context.route(route);
        context.getFakeRequest().uri("/post/2");

        Body body = parser.invoke(context, Body.class);
        assertThat(body.name).isEqualToIgnoringCase("wisdom");
        assertThat(body.id).isEqualTo(0);
        assertThat(body.time).isEqualTo(1000l);
        assertThat(body.flavors).containsExactly("a", "b");
        assertThat(body.path).isEqualTo(2);
    }

    @Test
    public void testThatInstancesCanBeCreatedFromFormParametersAndFileItems() {
        FakeContext context = new FakeContext()
                .setFormField("name", "wisdom")
                .setFormField("id", "0")
                .setFormField("time", "1000")
                .setFormField("flavors", "a", "b")
                .setFormField("item", new File("src/test/resources/a_file.txt"))
                .setFormField("content", new File("src/test/resources/a_file.txt"))
                .setFormField("stream", new File("src/test/resources/a_file.txt"))
                .setFormField("file_not_bound", new File("src/test/resources/a_file.txt"));
        context.route(route);
        context.getFakeRequest().uri("/post/2");

        BodyWithFiles body = parser.invoke(context, BodyWithFiles.class);
        assertThat(body.name).isEqualToIgnoringCase("wisdom");
        assertThat(body.id).isEqualTo(0);
        assertThat(body.time).isEqualTo(1000l);
        assertThat(body.flavors).containsExactly("a", "b");

        assertThat(body.item).isNotNull();
        assertThat(body.item.name()).isEqualTo("a_file.txt");
        assertThat(body.item.size()).isEqualTo(29);

        assertThat(body.content).isNotEmpty().hasSize(29);

        assertThat(body.stream).isNotNull();
    }

    @Test
    public void testThatWeCannotCreateInstancesFromClassesWithoutAnEmptyConstructor() {
        FakeContext context = new FakeContext()
                .setParameter("name", "wisdom")
                .setParameter("id", "0")
                .setParameter("time", "1000")
                .setParameter("flavors", ImmutableList.of("a", "b"));

        assertThat(parser.invoke(context, Stuff.class)).isNull();
    }

    public static class Stuff {
        private final String x;

        public Stuff(String x) {
            this.x = x;
        }
    }


    public static class Body {
        public String name;
        public int id;
        public long time;
        public String[] flavors;

        public int path;
    }

    public static class BodyWithSetter {
        private String name;
        private int id;
        private long time;
        private String[] flavors;

        private int path;

        public void setFlavors(String[] flavors) {
            this.flavors = flavors;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setPath(int path) {
            this.path = path;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void setFoo(String foo) {
            this.name = foo + "-set";
        }
    }

    public static class BodyWithFiles extends Body {
        FileItem item;
        byte[] content;
        InputStream stream;
    }

}