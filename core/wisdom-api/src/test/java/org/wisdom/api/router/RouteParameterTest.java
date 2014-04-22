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
package org.wisdom.api.router;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.wisdom.api.http.Context;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the construction of the parameters and attributes.
 */
public class RouteParameterTest {


    @Test
    public void testParameterFromPath() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn("hello");
        RouteUtils.Argument argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, String.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo("hello");

        when(ctx.parameterFromPath("param")).thenReturn("1");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Integer.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(1);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Integer.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(1);

        when(ctx.parameterFromPath("param")).thenReturn("true");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("on");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("yes");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("1");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("false");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);

        when(ctx.parameterFromPath("param")).thenReturn("off");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);

        when(ctx.parameterFromPath("param")).thenReturn("0");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);
    }

    @Test
    public void testParameterFromContext() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn(null);
        when(ctx.parameter("param")).thenReturn("hello");
        RouteUtils.Argument argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, String.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo("hello");

        when(ctx.parameter("param")).thenReturn("1");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Integer.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(1);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Integer.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(1);

        when(ctx.parameter("param")).thenReturn("true");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);

        when(ctx.parameter("param")).thenReturn("yes");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(true);

        when(ctx.parameter("param")).thenReturn("false");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);

        when(ctx.parameter("param")).thenReturn("no");
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.class);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.PARAMETER, Boolean.TYPE);
        assertThat(RouteUtils.getParameter(argument, ctx)).isEqualTo(false);
    }

    @Test
    public void testAttributeFromContext() {
        Context ctx = mock(Context.class);

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("hello")));
        RouteUtils.Argument argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, String.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo("hello");

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("1")));
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Integer.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(1);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Integer.TYPE);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(1);

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of());
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Integer.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(0);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Integer.TYPE);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(0);

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("true")));
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.TYPE);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(true);

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("yes")));
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(true);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.TYPE);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(true);

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of());
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.TYPE);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(false);

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("false")));
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.TYPE);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(false);

        when(ctx.attributes()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("0")));
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.class);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(false);
        argument = new RouteUtils.Argument("param", RouteUtils.Source.ATTRIBUTE, Boolean.TYPE);
        assertThat(RouteUtils.getAttribute(argument, ctx)).isEqualTo(false);
    }
}
