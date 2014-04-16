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
package org.wisdom.api;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.templates.Template;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * Checks the default controller.
 */
public class DefaultControllerTest {

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
    }

    @Test
    public void testAccessToContext() {
        EmptyController controller = new EmptyController();
        Context context = mock(Context.class);
        Context.CONTEXT.set(context);
        assertThat(controller.context()).isSameAs(context);
    }

    @Test(expected = IllegalStateException.class)
    public void testAccessToContextWhenContextNotSet() {
        EmptyController controller = new EmptyController();
        controller.context();
    }

    @Test
    public void testLoggerAccess() {
        EmptyController controller = new EmptyController();
        assertThat(controller.logger()).isNotNull();
        assertThat(controller.logger().getName()).isEqualTo(EmptyController.class.getName());
        controller.logger().info("test logging");
        assertThat(controller.logger()).isSameAs(controller.logger());
    }

    @Test
    public void testAccessToRequestFlashAndSession() {
        EmptyController controller = new EmptyController();
        Context context = mock(Context.class);
        Context.CONTEXT.set(context);
        Request request = mock(Request.class);
        SessionCookie session = mock(SessionCookie.class);
        FlashCookie flash = mock(FlashCookie.class);
        when(context.request()).thenReturn(request);
        when(context.session()).thenReturn(session);
        when(context.flash()).thenReturn(flash);

        assertThat(controller.context()).isSameAs(context);
        assertThat(controller.session()).isSameAs(session);
        assertThat(controller.flash()).isSameAs(flash);
        assertThat(controller.request()).isSameAs(request);

        // Try to set data and verify
        controller.flash("foo", "bar");
        verify(controller.flash(), times(1)).put("foo", "bar");
        controller.flash("foo");
        verify(controller.flash(), times(1)).get("foo");

        controller.session("foo", "bar");
        verify(controller.session(), times(1)).put("foo", "bar");
        controller.session("foo");
        verify(controller.session(), times(1)).get("foo");
    }

    @Test
    public void testEmptyTemplateRendering() {
        EmptyController controller = new EmptyController();
        Context context = mock(Context.class);
        Context.CONTEXT.set(context);
        Template template = mock(Template.class);
        controller.render(template);
        verify(template, times(1)).render(controller);
    }

    @Test
    public void testTemplateRenderingWithMap() {
        EmptyController controller = new EmptyController();
        Context context = mock(Context.class);
        Context.CONTEXT.set(context);
        Template template = mock(Template.class);
        Map<String, Object> parameters = ImmutableMap.<String, Object>of("foo", "bar");
        controller.render(template, parameters);
        verify(template, times(1)).render(controller, parameters);
    }

    @Test
    public void testTemplateRenderingWithVarAgs() {
        EmptyController controller = new EmptyController();
        Context context = mock(Context.class);
        Context.CONTEXT.set(context);
        Template template = mock(Template.class);
        Object[] parameters = new Object[]{"foo", "bar", "baz", 1};
        controller.render(template, parameters);
        //noinspection unchecked
        verify(template, times(1)).render(any(Controller.class), any(Map.class));
    }

    @Test
    public void testIllegalTemplateRenderingWithVarAgs() {
        EmptyController controller = new EmptyController();
        Context context = mock(Context.class);
        Context.CONTEXT.set(context);
        Template template = mock(Template.class);
        // Bad key, the third argument must be a String
        Object[] parameters = new Object[]{"foo", "bar", 1, "baz"};
        try {
            controller.render(template, parameters);
            fail("The third argument should have been seen as illegal");
        } catch (IllegalArgumentException e) {
            // ok.
        }

        // Illegal number of argument
        parameters = new Object[]{"foo", "bar", "baz"};
        try {
            controller.render(template, parameters);
            fail("The fourth argument is missing, it should have been detected");
        } catch (IllegalArgumentException e) {
            // ok.
        }

    }


    private class EmptyController extends DefaultController {

    }
}
