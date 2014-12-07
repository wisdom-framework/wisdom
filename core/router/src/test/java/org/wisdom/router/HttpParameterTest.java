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
import com.google.inject.util.Types;
import org.junit.Test;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.api.router.parameters.Source;
import org.wisdom.content.converters.ParamConverterEngine;
import org.wisdom.router.parameter.Bindings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the HTTP Parameters.
 */
public class HttpParameterTest {

    private ParameterFactories engine =
            new ParamConverterEngine(
                    Collections.<ParameterConverter>emptyList(),
                    Collections.<ParameterFactory>emptyList());

    @Test
    public void testContext() {
        Context ctx = mock(Context.class);
        ActionParameter argument = new ActionParameter(null, Source.HTTP, Context.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(ctx);
    }

    @Test
    public void testRequest() {
        Context ctx = mock(Context.class);
        Request request = mock(Request.class);
        when(ctx.request()).thenReturn(request);
        ActionParameter argument = new ActionParameter(null, Source.HTTP, Request.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(request);
    }

    @Test
    public void testRoute() {
        Context ctx = mock(Context.class);
        Route route = mock(Route.class);
        when(ctx.route()).thenReturn(route);
        ActionParameter argument = new ActionParameter(null, Source.HTTP, Route.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(route);
    }

    @Test
    public void testSessionCookie() {
        Context ctx = mock(Context.class);
        SessionCookie cookie = mock(SessionCookie.class);
        when(ctx.session()).thenReturn(cookie);
        ActionParameter argument = new ActionParameter(null, Source.HTTP, SessionCookie.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(cookie);
    }

    @Test
    public void testFlashCookie() {
        Context ctx = mock(Context.class);
        FlashCookie cookie = mock(FlashCookie.class);
        when(ctx.flash()).thenReturn(cookie);
        ActionParameter argument = new ActionParameter(null, Source.HTTP, FlashCookie.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(cookie);
    }

    @Test
    public void testCookie() {
        Context ctx = mock(Context.class);
        Cookie cookie = mock(Cookie.class);
        when(ctx.cookie("cookie")).thenReturn(cookie);
        ActionParameter argument = new ActionParameter("cookie", Source.HTTP, Cookie.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(cookie);
    }

    @Test
    public void testReader() throws IOException {
        Context ctx = mock(Context.class);
        BufferedReader reader = mock(BufferedReader.class);
        when(ctx.reader()).thenReturn(reader);
        ActionParameter argument = new ActionParameter(null, Source.HTTP, Reader.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(reader);

        argument = new ActionParameter(null, Source.HTTP, BufferedReader.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(reader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCookieWithoutName() {
        Context ctx = mock(Context.class);
        Cookie cookie = mock(Cookie.class);
        when(ctx.cookie("cookie")).thenReturn(cookie);
        ActionParameter argument = new ActionParameter(null, Source.HTTP, Cookie.class);
        Bindings.create(argument, ctx, engine);
        fail("Unexpected creation of object");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCookieWithEmptyName() {
        Context ctx = mock(Context.class);
        Cookie cookie = mock(Cookie.class);
        when(ctx.cookie("cookie")).thenReturn(cookie);
        ActionParameter argument = new ActionParameter("", Source.HTTP, Cookie.class);
        Bindings.create(argument, ctx, engine);
        fail("Unexpected creation of object");
    }

    @Test
    public void testMissingCookie() {
        Context ctx = mock(Context.class);
        Cookie cookie = mock(Cookie.class);
        when(ctx.cookie("cookie")).thenReturn(cookie);
        ActionParameter argument = new ActionParameter("missing", Source.HTTP, Cookie.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(null);
    }

    @Test
    public void testHeader() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(Collections.<String, Object>emptyMap());
        when(ctx.headers("header")).thenReturn(ImmutableList.of("value"));
        when(ctx.header("header")).thenReturn("value");
        when(ctx.headers("count")).thenReturn(ImmutableList.of("1"));
        when(ctx.header("count")).thenReturn("1");
        ActionParameter argument = new ActionParameter("header", Source.HTTP, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("value");
        argument = new ActionParameter("count", Source.HTTP, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("count", Source.HTTP, Integer.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("count", Source.HTTP, Long.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1l);
    }

    @Test
    public void testHeaderWithMultipleValues() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(Collections.<String, Object>emptyMap());
        when(ctx.headers("header")).thenReturn(ImmutableList.of("value1", "value2"));
        when(ctx.header("header")).thenReturn("value1");
        when(ctx.headers("count")).thenReturn(ImmutableList.of("1"));
        when(ctx.header("count")).thenReturn("1");
        ActionParameter argument = new ActionParameter("header", Source.HTTP, List.class, Types.listOf(String.class));
        assertThat((List) Bindings.create(argument, ctx, engine)).contains("value1", "value2");
        argument = new ActionParameter("header", Source.HTTP, String.class, null);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("value1");
        argument = new ActionParameter("count", Source.HTTP, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("count", Source.HTTP, List.class, Types.listOf(Integer.class));
        assertThat((List) Bindings.create(argument, ctx, engine)).containsExactly(1);
    }

    @Test
    public void testMissingHeader() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(Collections.<String, Object>emptyMap());
        when(ctx.headers("header")).thenReturn(ImmutableList.of("value1", "value2"));
        when(ctx.header("header")).thenReturn("value1");
        when(ctx.headers("count")).thenReturn(ImmutableList.of("1"));
        when(ctx.header("count")).thenReturn("1");
        ActionParameter argument = new ActionParameter("missing", Source.HTTP, List.class, Types.listOf(String.class));
        assertThat((List) Bindings.create(argument, ctx, engine)).isEmpty();
        argument = new ActionParameter("missing", Source.HTTP, String.class, null);
        assertThat((List) Bindings.create(argument, ctx, engine)).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderWithoutName() {
        Context ctx = mock(Context.class);
        when(ctx.headers("header")).thenReturn(ImmutableList.of("value1", "value2"));
        when(ctx.header("header")).thenReturn("value1");
        when(ctx.headers("count")).thenReturn(ImmutableList.of("1"));
        when(ctx.header("count")).thenReturn("1");
        ActionParameter argument = new ActionParameter(null, Source.HTTP, List.class, Types.listOf(String.class));
        Bindings.create(argument, ctx, engine);
        fail("Should have failed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderWithEmptyName() {
        Context ctx = mock(Context.class);
        when(ctx.headers("header")).thenReturn(ImmutableList.of("value1", "value2"));
        when(ctx.header("header")).thenReturn("value1");
        when(ctx.headers("count")).thenReturn(ImmutableList.of("1"));
        when(ctx.header("count")).thenReturn("1");
        ActionParameter argument = new ActionParameter("", Source.HTTP, List.class, Types.listOf(String.class));
        Bindings.create(argument, ctx, engine);
        fail("Should have failed");
    }

    @Test
    public void testRequestScopeInjection() throws MalformedURLException {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        final URL url = new URL("http://perdu.com");
        when(request.data()).thenReturn(ImmutableMap.<String, Object>of(
                "data", url,
                "key", "value",
                "count", 1
        ));
        ActionParameter argument = new ActionParameter("data", Source.HTTP, URL.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(url);
        argument = new ActionParameter("key", Source.HTTP, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("value");
        argument = new ActionParameter("count", Source.HTTP, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
    }

    @Test
    public void testRequestScopeInjectionWithMultipleValues() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(ImmutableMap.<String, Object>of(
                "data", ImmutableList.of("value1", "value2"),
                "key", "value",
                "count", 1
        ));
        ActionParameter argument = new ActionParameter("data", Source.HTTP, List.class, Types.listOf(String.class));
        assertThat((List) Bindings.create(argument, ctx, engine)).contains("value1", "value2");
    }

    @Test
    public void testMissingRequestScopeValue() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(ImmutableMap.<String, Object>of(
                "data", ImmutableList.of("value1", "value2"),
                "key", "value",
                "count", 1
        ));
        ActionParameter argument = new ActionParameter("missing", Source.HTTP, List.class, Types.listOf(String.class));
        assertThat((List) Bindings.create(argument, ctx, engine)).isEmpty();
        argument = new ActionParameter("missing", Source.HTTP, String.class, null);
        assertThat((List) Bindings.create(argument, ctx, engine)).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestScopeInjectionWithoutName() {
        Request request = mock(Request.class);
        Context ctx = mock(Context.class);
        when(ctx.request()).thenReturn(request);
        when(request.data()).thenReturn(ImmutableMap.<String, Object>of(
                "data", ImmutableList.of("value1", "value2"),
                "key", "value",
                "count", 1
        ));
        ActionParameter argument = new ActionParameter(null, Source.HTTP, List.class, Types.listOf(String.class));
        Bindings.create(argument, ctx, engine);
        fail("Should have failed");
    }


}
