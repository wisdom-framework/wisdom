/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.framework.csrf;

import com.google.common.collect.ImmutableSet;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.thymeleaf.Arguments;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.wisdom.api.templates.TemplateEngine;
import org.wisdom.framework.csrf.api.CSRFService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Exposed the csrf dialect for thymeleaf injecting a hidden input field in form containing a CSRF token. As this is
 * only available for Thmeleaf, this component publishes the dialect only if the Thymeleaf template engine is available.
 */
@Component(immediate = true)
@Instantiate
public class ThymeleafCsrfDialect {

    @Context
    BundleContext context;

    @Requires
    CSRFService csrf;

    ServiceRegistration<IDialect> reg;


    @Bind(aggregate = true)
    public void bindTemplateEngine(TemplateEngine engine) {
        if (engine.name().equals("thymeleaf")) {
            publishDialect();
        }
    }

    private void publishDialect() {
        reg = context.registerService(IDialect.class, createDialect(), null);
    }

    protected IDialect createDialect() {
        return new AbstractDialect() {
            @Override
            public String getPrefix() {
                return "csrf";
            }

            @Override
            public Set<IProcessor> getProcessors() {
                return ImmutableSet.<IProcessor>of(new CSRFElementProcessor());
            }
        };
    }

    @Unbind
    public void unbindTemplateEngine(TemplateEngine engine) {
        if (engine.name().equals("thymeleaf")) {
            unpublishDialect();
        }
    }

    private void unpublishDialect() {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
    }


    class CSRFElementProcessor extends AbstractMarkupSubstitutionElementProcessor {
        public CSRFElementProcessor() {
            super("token");
        }

        @Override
        public int getPrecedence() {
            return 1000;
        }

        @Override
        protected List<Node> getMarkupSubstitutes(
                final Arguments arguments, final Element element) {
            final Element input = new Element("input");
            input.setAttribute("type", "hidden");
            input.setAttribute("name", csrf.getTokenName());
            final String token = csrf.getCurrentToken(org.wisdom.api.http.Context.CONTEXT.get());
            if (token != null) {
                input.setAttribute("value", token);
            } else {
                input.setAttribute("value", "invalid");
            }
            final List<Node> nodes = new ArrayList<>();
            nodes.add(input);
            return nodes;
        }
    }
}
