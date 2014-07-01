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
package org.wisdom.template.thymeleaf.impl;

import com.google.common.collect.ImmutableMap;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.wisdom.api.Controller;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.wisdom.template.thymeleaf.dialect.Routes;
import org.wisdom.template.thymeleaf.dialect.WisdomStandardDialect;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.FakeContext;
import org.wisdom.test.parents.Invocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wisdom.api.http.Results.ok;
import static org.wisdom.test.parents.Action.action;


/**
 * Check template processing.
 */
public class WisdomTemplateEngineTest {

    @Test
    public void testJavaScript() {
        TemplateEngine engine = createWisdomEngine();
        Context context = new Context();
        context.setVariable("test", "test");

        FakeRouter router = new FakeRouter();
        Controller controller = new FakeController();
        router.addController(controller);

        Assets assets = mock(Assets.class);

        context.setVariable(Routes.ROUTES_VAR, new Routes(router, assets, controller));

        String processed = engine.process("templates/javascript.thl.html", context);
        assertThat(processed).containsIgnoringCase("var t = 'test';");
        assertThat(processed).containsIgnoringCase("var url = '/';");
        assertThat(processed).containsIgnoringCase("$(document).ready(function () {");
    }

    @Test
    public void testSessionScope() {
        final WisdomTemplateEngine engine = createWisdomEngine();
        final Template template = mock(Template.class);
        when(template.fullName()).thenReturn("templates/var.thl.html");

        final FakeRouter router = new FakeRouter();
        final Controller controller = new FakeController();
        router.addController(controller);
        final Assets assets = mock(Assets.class);


        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return ok(engine.process(template, controller, router, assets, ImmutableMap.<String, Object>of("key",
                        "test")));
            }
        }).with(new FakeContext().addToSession("key2", "session")).invoke();

        String content = (String) result.getResult().getRenderable().content();
        assertThat(content)
                .contains("<span>KEY</span> = <span>test</span>")
                .contains("<span>KEY2</span> = <span>session</span>");
    }

    @Test
    public void testFlashScope() {
        final WisdomTemplateEngine engine = createWisdomEngine();
        final Template template = mock(Template.class);
        when(template.fullName()).thenReturn("templates/var.thl.html");

        final FakeRouter router = new FakeRouter();
        final Controller controller = new FakeController();
        router.addController(controller);
        final Assets assets = mock(Assets.class);


        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                context().flash().put("key2", "ongoing");
                return ok(engine.process(template, controller, router, assets, ImmutableMap.<String, Object>of()));
            }
        }).with(new FakeContext().addToFlash("key", "incoming")).invoke();

        String content = (String) result.getResult().getRenderable().content();
        assertThat(content)
                .contains("<span>KEY</span> = <span>incoming</span>")
                .contains("<span>KEY2</span> = <span>ongoing</span>");
    }

    @Test
    public void testParameter() {
        final WisdomTemplateEngine engine = createWisdomEngine();
        final Template template = mock(Template.class);
        when(template.fullName()).thenReturn("templates/var.thl.html");

        final FakeRouter router = new FakeRouter();
        final Controller controller = new FakeController();
        router.addController(controller);
        final Assets assets = mock(Assets.class);


        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                context().session().put("key2", "ongoing");
                return ok(engine.process(template, controller, router, assets, ImmutableMap.<String, Object>of()));
            }
        }).parameter("key", "param").invoke();

        String content = (String) result.getResult().getRenderable().content();
        assertThat(content)
                .contains("<span>KEY</span> = <span>param</span>")
                .contains("<span>KEY2</span> = <span>ongoing</span>");
    }

    private WisdomTemplateEngine createWisdomEngine() {
        WisdomTemplateEngine engine = new WisdomTemplateEngine();
        engine.setTemplateResolver(new ClassLoaderTemplateResolver());

        // We clear the dialects as we are using our own standard dialect.
        engine.clearDialects();
        engine.addDialect(new WisdomStandardDialect());
        engine.addDialect(new LayoutDialect());

        engine.initialize();
        return engine;
    }

}
