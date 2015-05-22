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

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.wisdom.api.Controller;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.bodies.RenderableString;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.template.thymeleaf.dialect.Routes;
import org.wisdom.template.thymeleaf.dialect.WisdomStandardDialect;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main integration point of Thymeleaf in wisdom.
 */
public class WisdomTemplateEngine extends TemplateEngine {

    public WisdomTemplateEngine(Set<IDialect> dialects) {
        super();
        // We clear the dialects as we are using our own standard dialect.
        clearDialects();
        addDialect(new WisdomStandardDialect());
        addDialect(new LayoutDialect());

        if (dialects != null) {
            setAdditionalDialects(dialects);
        }
    }

    /**
     * Renders the given template.
     * <p>
     * Variables from the session, flash and request parameters are added to the given parameters.
     *
     * @param template   the template
     * @param controller the template asking for the rendering
     * @param router     the router service
     * @param variables  the template parameters
     * @return the rendered HTML page
     */
    public RenderableString process(Template template, Controller controller, Router router, Assets assets, Map<String,
            Object> variables) {
        Context ctx = new Context();
        // Add session
        final org.wisdom.api.http.Context http = org.wisdom.api.http.Context.CONTEXT.get();
        ctx.setVariables(http.session().getData());
        // Add flash
        ctx.setVariables(http.flash().getCurrentFlashCookieData());
        ctx.setVariables(http.flash().getOutgoingFlashCookieData());

        // Add parameter from request, flattened
        for (Map.Entry<String, List<String>> entry : http.parameters().entrySet()) {
            if (entry.getValue().size() == 1) {
                ctx.setVariable(entry.getKey(), entry.getValue().get(0));
            } else {
                ctx.setVariable(entry.getKey(), entry.getValue());
            }
        }

        // Add request scope
        for (Map.Entry<String, Object> entry : http.request().data().entrySet()) {
            ctx.setVariable(entry.getKey(), entry.getValue());
        }

        // Add variable.
        ctx.setVariables(variables);
        ctx.setVariable(Routes.ROUTES_VAR, new Routes(router, assets, controller));
        // This variable let us resolve template using relative path (in the same directory as the current template).
        // It's mainly used for 'layout', so we can compute the full url.
        ctx.setVariable("__TEMPLATE__", template);
        StringWriter writer = new StringWriter();
        try {
            this.process(template.fullName(), ctx, writer);
        } catch (TemplateProcessingException e) {
            // If we have a nested cause having a nested cause, heuristics say that it's the useful message.
            // Rebuild an exception using this data.
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw new TemplateProcessingException(e.getCause().getCause().getMessage(),
                        e.getTemplateName(),
                        e.getLineNumber(),
                        e.getCause().getCause());
            } else {
                throw e;
            }
        }
        return new RenderableString(writer, MimeTypes.HTML);
    }

}
