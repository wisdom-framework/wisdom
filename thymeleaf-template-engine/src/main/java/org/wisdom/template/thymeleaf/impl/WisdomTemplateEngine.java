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

import org.wisdom.api.Controller;
import org.wisdom.api.bodies.RenderableString;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.template.thymeleaf.dialect.Routes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * The main integration point of thymeleaf in wisdom.
 */
public class WisdomTemplateEngine extends TemplateEngine {

    public RenderableString process(Template template, Controller controller, Router router, Map<String,
            Object> variables) {
        Context ctx = new Context();
        // Add session
        ctx.setVariables(org.wisdom.api.http.Context.CONTEXT.get().session().getData());
        // Add flash
        ctx.setVariables(org.wisdom.api.http.Context.CONTEXT.get().flash().getCurrentFlashCookieData());

        // Add parameter from request, flattened
        for (Map.Entry<String, List<String>> entry : org.wisdom.api.http.Context.CONTEXT.get()
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
