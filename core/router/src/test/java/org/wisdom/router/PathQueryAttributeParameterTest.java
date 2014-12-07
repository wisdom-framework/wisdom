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
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.api.http.Context;
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
 * Check the construction of the parameters and attributes.
 */
public class PathQueryAttributeParameterTest {

    private ParameterFactories engine =
            new ParamConverterEngine(
                    Collections.<ParameterConverter>emptyList(),
                    Collections.<ParameterFactory>emptyList());

    @Test
    public void testParameterFromPath() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn("hello");
        ActionParameter argument = new ActionParameter("param", Source.PARAMETER, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("hello");

        when(ctx.parameterFromPath("param")).thenReturn("1");
        argument = new ActionParameter("param", Source.PARAMETER, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("param", Source.PARAMETER, Integer.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);

        when(ctx.parameterFromPath("param")).thenReturn("true");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("on");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("yes");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("1");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("false");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.parameterFromPath("param")).thenReturn("off");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.parameterFromPath("param")).thenReturn("0");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
    }

    @Test
    public void testPathParameter() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn("hello");
        ActionParameter argument = new ActionParameter("param", Source.PATH, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("hello");

        when(ctx.parameterFromPath("param")).thenReturn("1");
        argument = new ActionParameter("param", Source.PATH, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("param", Source.PATH, Integer.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);

        when(ctx.parameterFromPath("param")).thenReturn("true");
        argument = new ActionParameter("param", Source.PATH, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PATH, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("on");
        argument = new ActionParameter("param", Source.PATH, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PATH, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("yes");
        argument = new ActionParameter("param", Source.PATH, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PATH, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("1");
        argument = new ActionParameter("param", Source.PATH, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PATH, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameterFromPath("param")).thenReturn("false");
        argument = new ActionParameter("param", Source.PATH, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PATH, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.parameterFromPath("param")).thenReturn("off");
        argument = new ActionParameter("param", Source.PATH, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PATH, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.parameterFromPath("param")).thenReturn("0");
        argument = new ActionParameter("param", Source.PATH, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PATH, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
    }

    @Test
    public void testParameterFromQuery() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn(null);
        when(ctx.parameter("param")).thenReturn("hello");
        ActionParameter argument = new ActionParameter("param", Source.PARAMETER, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("hello");

        when(ctx.parameter("param")).thenReturn("1");
        argument = new ActionParameter("param", Source.PARAMETER, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("param", Source.PARAMETER, Integer.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);

        when(ctx.parameter("param")).thenReturn("true");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameter("param")).thenReturn("yes");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameter("param")).thenReturn("false");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.parameter("param")).thenReturn("no");
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.PARAMETER, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
    }

    @Test
    public void testQueryParameter() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn(null);
        when(ctx.parameter("param")).thenReturn("hello");
        ActionParameter argument = new ActionParameter("param", Source.QUERY, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("hello");

        when(ctx.parameter("param")).thenReturn("1");
        argument = new ActionParameter("param", Source.QUERY, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("param", Source.QUERY, Integer.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);

        when(ctx.parameter("param")).thenReturn("true");
        argument = new ActionParameter("param", Source.QUERY, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.QUERY, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameter("param")).thenReturn("yes");
        argument = new ActionParameter("param", Source.QUERY, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.QUERY, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.parameter("param")).thenReturn("false");
        argument = new ActionParameter("param", Source.QUERY, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.QUERY, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.parameter("param")).thenReturn("no");
        argument = new ActionParameter("param", Source.QUERY, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.QUERY, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
    }

    @Test
    public void testQueryAndPathParameter() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn("path");
        when(ctx.parameter("param")).thenReturn("query");
        ActionParameter argument = new ActionParameter("param", Source.QUERY, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("query");

        argument = new ActionParameter("param", Source.PATH, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("path");

        when(ctx.parameterFromPath("param")).thenReturn(null);
        when(ctx.parameter("param")).thenReturn(null);
        argument = new ActionParameter("param", Source.PATH, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(null);

        argument = new ActionParameter("param", Source.QUERY, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(null);

        argument = new ActionParameter("param", Source.QUERY, String.class);
        argument.setDefaultValue("default");
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("default");

        argument = new ActionParameter("param", Source.PATH, String.class);
        argument.setDefaultValue("default");
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("default");
    }

    @Test
    public void testMultipleParameters() {
        Context ctx = mock(Context.class);

        when(ctx.parameterFromPath("param")).thenReturn(null);

        when(ctx.parameterMultipleValues("param")).thenReturn(ImmutableList.of("hello", "world"));
        ActionParameter argument = new ActionParameter("param", Source.PARAMETER, String[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).containsExactly("hello", "world");

        when(ctx.parameterMultipleValues("param")).thenReturn(Collections.<String>emptyList());
        argument = new ActionParameter("param", Source.PARAMETER, String[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).hasSize(0);

        when(ctx.parameterMultipleValues("param")).thenReturn(null);
        argument = new ActionParameter("param", Source.PARAMETER, String[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).hasSize(0);

        when(ctx.parameterMultipleValues("param")).thenReturn(ImmutableList.of("1", "2", "3"));
        argument = new ActionParameter("param", Source.PARAMETER, Integer[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).containsExactly(1, 2, 3);

        when(ctx.parameterMultipleValues("param")).thenReturn(null);
        argument = new ActionParameter("param", Source.PARAMETER, Integer[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).hasSize(0);

        when(ctx.parameterMultipleValues("param")).thenReturn(ImmutableList.of("true", "on", "off", "false", "1", "0"));
        argument = new ActionParameter("param", Source.PARAMETER, Boolean[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).containsExactly(true, true, false, false,
                true, false);

        when(ctx.parameterMultipleValues("param")).thenReturn(Collections.<String>emptyList());
        argument = new ActionParameter("param", Source.PARAMETER, Boolean[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).hasSize(0);
    }

    @Test
    public void testAttributeFromContext() {
        Context ctx = mock(Context.class);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("hello")));
        ActionParameter argument = new ActionParameter("param", Source.FORM, String.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo("hello");

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("1")));
        argument = new ActionParameter("param", Source.FORM, Integer.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);
        argument = new ActionParameter("param", Source.FORM, Integer.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(1);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of());
        // If int is used we return 0
        argument = new ActionParameter("param", Source.FORM, Integer.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(0);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("true")));
        argument = new ActionParameter("param", Source.FORM, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.FORM, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("yes")));
        argument = new ActionParameter("param", Source.FORM, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);
        argument = new ActionParameter("param", Source.FORM, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(true);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of());
        argument = new ActionParameter("param", Source.FORM, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.FORM, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("false")));
        argument = new ActionParameter("param", Source.FORM, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.FORM, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("0")));
        argument = new ActionParameter("param", Source.FORM, Boolean.class);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
        argument = new ActionParameter("param", Source.FORM, Boolean.TYPE);
        assertThat(Bindings.create(argument, ctx, engine)).isEqualTo(false);
    }

    @Test
    public void testMultipleFormValues() {
        Context ctx = mock(Context.class);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("hello", "world")));
        ActionParameter argument = new ActionParameter("param", Source.FORM, String[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).containsExactly("hello", "world");

        when(ctx.form()).thenReturn(ImmutableMap.of("param",
                Collections.<String>emptyList()));
        argument = new ActionParameter("param", Source.FORM, String[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).hasSize(0);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("1", "2", "3")));
        argument = new ActionParameter("param", Source.FORM, Integer[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).containsExactly(1, 2, 3);

        when(ctx.form()).thenReturn(ImmutableMap.<String, List<String>>of("param", ImmutableList.of("true",
                "on", "off", "false", "1", "0")));
        argument = new ActionParameter("param", Source.FORM, Boolean[].class);
        assertThat((Object[]) (Bindings.create(argument, ctx, engine))).containsExactly(true, true, false, false,
                true, false);
    }
}
