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
package org.wisdom.router;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.api.router.parameters.Source;
import org.wisdom.content.converters.ParamConverterEngine;
import org.wisdom.router.parameter.Bindings;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the Bean Parameters.
 */
public class BeanParameterTest {

    private ParameterFactories engine =
            new ParamConverterEngine(
                    Collections.<ParameterConverter>emptyList(),
                    Collections.<ParameterFactory>emptyList());

    @Test
    public void testSmallBean() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(Collections.<String, Object>emptyMap());
        when(ctx.headers("header")).thenReturn(ImmutableList.of("head"));
        when(ctx.header("header")).thenReturn("head");
        when(ctx.parameter("q")).thenReturn("1");
        ActionParameter argument = new ActionParameter(null, Source.BEAN, SmallBean.class);
        SmallBean bean = (SmallBean) Bindings.create(argument, ctx, engine);
        assertThat(bean).isNotNull();
        assertThat(bean.headerParam).isEqualTo("head");
        assertThat(bean.q).isEqualTo(1);
    }

    @Test
    public void testFullBean() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(Collections.<String, Object>emptyMap());
        when(ctx.headers("header")).thenReturn(ImmutableList.of("head"));
        when(ctx.header("header")).thenReturn("head");
        when(ctx.parameter("q")).thenReturn("1");
        when(ctx.parameterFromPath("p")).thenReturn("item");
        when(ctx.form())
                .thenReturn(ImmutableMap.<String, List<String>>of("form", ImmutableList.of("31")));
        Cookie cookie = mock(Cookie.class);
        when(ctx.cookie("cookie")).thenReturn(cookie);

        ActionParameter argument = new ActionParameter(null, Source.BEAN, FullBean.class);
        FullBean bean = (FullBean) Bindings.create(argument, ctx, engine);

        assertThat(bean.headerParam).isEqualTo("head");
        assertThat(bean.queryParam).isEqualTo(1);
        assertThat(bean.pathParam).isEqualTo("item");
        assertThat(bean.formParam).isEqualTo(31);
        assertThat(bean.cookie).isEqualTo(cookie);
        assertThat(bean.request).isEqualTo(request);
    }

    @Test
    public void testConstructorBean() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(Collections.<String, Object>emptyMap());
        when(ctx.headers("header")).thenReturn(ImmutableList.of("head"));
        when(ctx.header("header")).thenReturn("head");
        when(ctx.parameter("q")).thenReturn("1");
        when(ctx.parameterFromPath("p")).thenReturn("item");
        when(ctx.form())
                .thenReturn(ImmutableMap.<String, List<String>>of("form", ImmutableList.of("31")));
        Cookie cookie = mock(Cookie.class);
        when(ctx.cookie("cookie")).thenReturn(cookie);

        ActionParameter argument = new ActionParameter(null, Source.BEAN, ConstructorBean.class);
        ConstructorBean bean = (ConstructorBean) Bindings.create(argument, ctx, engine);

        assertThat(bean.headerParam).isEqualTo("head");
        assertThat(bean.queryParam).isEqualTo(1);
        assertThat(bean.pathParam).isEqualTo("item");
        assertThat(bean.formParam).isEqualTo(31);
        assertThat(bean.cookie).isEqualTo(cookie);
        assertThat(bean.request).isEqualTo(request);
    }

    @Test
    public void testNestedBean() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(Collections.<String, Object>emptyMap());
        when(ctx.headers("header")).thenReturn(ImmutableList.of("head"));
        when(ctx.header("header")).thenReturn("head");
        when(ctx.parameter("q")).thenReturn("1");
        when(ctx.parameterFromPath("p")).thenReturn("item");
        when(ctx.form())
                .thenReturn(ImmutableMap.<String, List<String>>of("form", ImmutableList.of("31")));
        Cookie cookie = mock(Cookie.class);
        when(ctx.cookie("cookie")).thenReturn(cookie);


        ActionParameter argument = new ActionParameter(null, Source.BEAN, NestedBean.class);
        NestedBean nb = (NestedBean) Bindings.create(argument, ctx, engine);

        FullBean bean = nb.full;
        assertThat(bean.headerParam).isEqualTo("head");
        assertThat(bean.queryParam).isEqualTo(1);
        assertThat(bean.pathParam).isEqualTo("item");
        assertThat(bean.formParam).isEqualTo(31);
        assertThat(bean.cookie).isEqualTo(cookie);
        assertThat(bean.request).isEqualTo(request);

        SmallBean sm = nb.small;
        assertThat(sm.headerParam).isEqualTo("head");
        assertThat(sm.q).isEqualTo(1);
    }

    public static class SmallBean {

        private String headerParam;
        private int q;

        public void setHeader(@HttpParameter("header") String h) {
            headerParam = h;
        }

        public void setPath(@QueryParameter("q") int v) {
            this.q = v;
        }

        public SmallBean() {
            // Empty constructor used by Wisdom
        }

        public SmallBean(String s) {
            // A constructor that should not be used.
        }

    }

    public static class FullBean {
        private String headerParam;

        private String pathParam;

        private int queryParam;

        private Cookie cookie;

        private int formParam;

        private Request request;


        public void setCookie(@HttpParameter("cookie") Cookie cookie) {
            this.cookie = cookie;
        }

        public void setFormParam(@FormParameter("form") int formParam) {
            this.formParam = formParam;
        }

        public void setHeaderParam(@HttpParameter("header") String headerParam) {
            this.headerParam = headerParam;
        }

        public void setPathParam(@PathParameter("p") String pathParam) {
            this.pathParam = pathParam;
        }

        public void setQueryParam(@QueryParameter("q") int queryParam) {
            this.queryParam = queryParam;
        }

        public void setRequest(@HttpParameter Request request) {
            this.request = request;
        }


        @Override
        public String toString() {
            return "Bean{" +
                    "cookie='" + cookie + '\'' +
                    ", formParam='" + formParam + '\'' +
                    ", headerParam='" + headerParam + '\'' +
                    ", pathParam='" + pathParam + '\'' +
                    ", queryParam='" + queryParam + '\'' +
                    '}';
        }

    }

    public static class NestedBean {

        FullBean full;
        SmallBean small;

        public void setFull(@BeanParameter FullBean f) {
            full = f;
        }

        public void setSmall(@BeanParameter SmallBean s) {
            small = s;
        }

    }

    public static class ConstructorBean {
        private String headerParam;

        private String pathParam;

        private int queryParam;

        private Cookie cookie;

        private int formParam;

        private Request request;

        public ConstructorBean(@HttpParameter("cookie") Cookie cookie,
                               @FormParameter("form") int formParam,
                               @HttpParameter("header") String headerParam,
                               @PathParameter("p") String pathParam,
                               @QueryParameter("q") int queryParam,
                               @HttpParameter Request request
        ) {
            this.cookie = cookie;
            this.formParam = formParam;
            this.headerParam = headerParam;
            this.pathParam = pathParam;
            this.request = request;
            this.queryParam = queryParam;
        }

    }
}
