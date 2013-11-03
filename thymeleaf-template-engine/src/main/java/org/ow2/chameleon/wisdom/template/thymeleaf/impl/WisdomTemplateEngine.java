package org.ow2.chameleon.wisdom.template.thymeleaf.impl;

import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.bodies.RenderableString;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;
import org.ow2.chameleon.wisdom.template.thymeleaf.dialect.Routes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * The main integration point of thymeleaf in wisdom.
 */
public class WisdomTemplateEngine extends TemplateEngine {


    // TODO Message resolver.

    public Renderable process(Template template, Controller controller, Router router, Map<String, Object> variables) {
        Context ctx = new Context();
        // Add session
        ctx.setVariables(org.ow2.chameleon.wisdom.api.http.Context.context.get().session().getData());
        // Add flash
        ctx.setVariables(org.ow2.chameleon.wisdom.api.http.Context.context.get().flash().getCurrentFlashCookieData());

        // Add parameter from request, flattened
        for (Map.Entry<String, List<String>> entry : org.ow2.chameleon.wisdom.api.http.Context.context.get()
                .parameters().entrySet()) {
            if (entry.getValue().size() == 1) {
                ctx.setVariable(entry.getKey(), entry.getValue().get(0));
            } else {
                ctx.setVariable(entry.getKey(), entry.getValue());
            }
        }

        // Add variable.
        ctx.setVariables(variables);
        ctx.setVariable(Routes.ROUTES_VAR, new Routes(router, controller));


        StringWriter writer = new StringWriter();
        this.process(template.fullName(), ctx, writer);
        return new RenderableString(writer, MimeTypes.HTML);
    }

}
