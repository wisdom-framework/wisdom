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

import org.junit.Test;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.DefaultValue;
import org.wisdom.api.annotations.FormParameter;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.api.router.parameters.Source;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that action parameter extractions.
 */
public class ActionParameterTest {

    // Method to test the simple cases
    public Result noop() { return Results.ok(); }
    public Result param(@Parameter("message") String message) { return Results.ok(); }
    public Result attribute(@FormParameter("message") String message) { return Results.ok(); }
    public Result file(@FormParameter("file") FileItem upload) { return Results.ok(); }
    public Result body(@Body() String message) { return Results.ok(); }

    // Generics
    public Result param(@Parameter("messages") List<String> messages) { return Results.ok(); }
    public Result param2(@Parameter("messages") List<Object> messages) { return Results.ok(); }

    // Default Value
    public Result paramWithDefault(@Parameter("message") @DefaultValue("hello") String message) { return Results.ok(); }
    public Result paramWithDefault(@Parameter("message") @DefaultValue("hello") List<String> message) { return
            Results.ok(); }
    public Result attributeWithDefault(@DefaultValue("hello") @FormParameter("message") String message) { return Results.ok
            (); }


    @Test
    public void testNoop() throws Throwable {
        Method method = this.getClass().getMethod("noop");
        assertThat(RouteUtils.buildActionParameterList(method)).isEmpty();
    }

    @Test
    public void testParamWithoutGenerics() throws Throwable {
        Method method = this.getClass().getMethod("param", String.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.PARAMETER);
        assertThat(parameter.getName()).isEqualTo("message");
        assertThat(parameter.getRawType()).isEqualTo(String.class);
        assertThat(parameter.getGenericType()).isEqualTo(String.class);
        assertThat(parameter.getDefaultValue()).isNull();
    }

    @Test
    public void testParamWithGenerics() throws Throwable {
        Method method = this.getClass().getMethod("param", List.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.PARAMETER);
        assertThat(parameter.getName()).isEqualTo("messages");
        assertThat(parameter.getRawType()).isEqualTo(List.class);
        assertThat(parameter.getGenericType().toString()).contains("java.util.List<java.lang.String>");
        assertThat(parameter.getDefaultValue()).isNull();
    }

    @Test
    public void testParam2WithGenerics() throws Throwable {
        Method method = this.getClass().getMethod("param2", List.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.PARAMETER);
        assertThat(parameter.getName()).isEqualTo("messages");
        assertThat(parameter.getRawType()).isEqualTo(List.class);
        assertThat(parameter.getGenericType().toString()).contains("java.util.List<java.lang.Object>");
        assertThat(parameter.getDefaultValue()).isNull();
    }

    @Test
    public void testAttribute() throws Throwable {
        Method method = this.getClass().getMethod("attribute", String.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.FORM);
        assertThat(parameter.getName()).isEqualTo("message");
        assertThat(parameter.getRawType()).isEqualTo(String.class);
        assertThat(parameter.getGenericType()).isEqualTo(String.class);
        assertThat(parameter.getDefaultValue()).isNull();
    }

    @Test
    public void testFile() throws Throwable {
        Method method = this.getClass().getMethod("file", FileItem.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.FORM);
        assertThat(parameter.getName()).isEqualTo("file");
        assertThat(parameter.getRawType()).isEqualTo(FileItem.class);
        assertThat(parameter.getGenericType()).isEqualTo(FileItem.class);
        assertThat(parameter.getDefaultValue()).isNull();
    }

    @Test
    public void testBody() throws Throwable {
        Method method = this.getClass().getMethod("body", String.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.BODY);
        assertThat(parameter.getName()).isNull();
        assertThat(parameter.getRawType()).isEqualTo(String.class);
        assertThat(parameter.getGenericType()).isEqualTo(String.class);
        assertThat(parameter.getDefaultValue()).isNull();
    }

    @Test
    public void testParamWithDefault() throws Throwable {
        Method method = this.getClass().getMethod("paramWithDefault", String.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.PARAMETER);
        assertThat(parameter.getName()).isEqualTo("message");
        assertThat(parameter.getRawType()).isEqualTo(String.class);
        assertThat(parameter.getGenericType()).isEqualTo(String.class);
        assertThat(parameter.getDefaultValue()).isEqualTo("hello");
    }

    @Test
    public void testParamWithDefaultAndGenerics() throws Throwable {
        Method method = this.getClass().getMethod("paramWithDefault", List.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.PARAMETER);
        assertThat(parameter.getName()).isEqualTo("message");
        assertThat(parameter.getRawType()).isEqualTo(List.class);
        assertThat(parameter.getGenericType().toString()).contains("java.util.List<java.lang.String>");
        assertThat(parameter.getDefaultValue()).isEqualTo("hello");
    }

    @Test
    public void testAttributeWithDefault() throws Throwable {
        Method method = this.getClass().getMethod("attributeWithDefault", String.class);
        assertThat(RouteUtils.buildActionParameterList(method)).hasSize(1);
        final ActionParameter parameter = RouteUtils.buildActionParameterList(method).get(0);
        assertThat(parameter.getSource()).isEqualTo(Source.FORM);
        assertThat(parameter.getName()).isEqualTo("message");
        assertThat(parameter.getRawType()).isEqualTo(String.class);
        assertThat(parameter.getGenericType()).isEqualTo(String.class);
        assertThat(parameter.getDefaultValue()).isEqualTo("hello");
    }
}
