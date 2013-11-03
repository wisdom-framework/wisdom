package org.ow2.chameleon.wisdom.template.thymeleaf.impl;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.junit.Test;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.template.thymeleaf.dialect.Routes;
import org.ow2.chameleon.wisdom.template.thymeleaf.dialect.WisdomStandardDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.fest.assertions.Assertions.assertThat;

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

        context.setVariable(Routes.ROUTES_VAR, new Routes(router, controller));

        String processed = engine.process("templates/javascript.html", context);
        assertThat(processed).containsIgnoringCase("var t = 'test';");
        assertThat(processed).containsIgnoringCase("var url = '/';");
        assertThat(processed).containsIgnoringCase("$(document).ready(function () {");
    }

    private TemplateEngine createWisdomEngine() {
        TemplateEngine engine = new WisdomTemplateEngine();
        engine.setTemplateResolver(new ClassLoaderTemplateResolver());

        // We clear the dialects as we are using our own standard dialect.
        engine.clearDialects();
        engine.addDialect(new WisdomStandardDialect());
        engine.addDialect(new LayoutDialect());

        engine.initialize();
        return engine;
    }

    private TemplateEngine createRegularEngine() {
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(new ClassLoaderTemplateResolver());

        engine.initialize();
        return engine;
    }

}
