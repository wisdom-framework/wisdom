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
package org.wisdom.template.thymeleaf;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.api.templates.TemplateEngine;
import org.wisdom.template.thymeleaf.impl.ThymeLeafTemplateImplementation;
import org.wisdom.template.thymeleaf.impl.WisdomTemplateEngine;
import org.wisdom.template.thymeleaf.impl.WisdomURLResourceResolver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main component of the Thymeleaf template engine integration in Wisdom.
 */
@Component(immediate = true)
@Provides(specifications = {ThymeleafTemplateCollector.class, TemplateEngine.class})
@Instantiate(name = "Thymeleaf template engine")
public class ThymeleafTemplateCollector implements TemplateEngine {

    /**
     * The extension of the template supported by this engine.
     */
    public static final String THYMELEAF_TEMPLATE_EXTENSION = "thl.html";

    /**
     * The name of the template engine.
     */
    public static final String THYMELEAF_ENGINE_NAME = "thymeleaf";

    @Requires
    IMessageResolver messageResolver;


    private final BundleContext context;

    @Requires
    ApplicationConfiguration configuration;

    private static final Logger LOGGER = LoggerFactory.getLogger(ThymeleafTemplateCollector.class.getName());

    private Map<ThymeLeafTemplateImplementation, ServiceRegistration<Template>> registrations = new ConcurrentHashMap<>();
    private WisdomTemplateEngine engine;

    @Requires
    private Router router;

    public ThymeleafTemplateCollector(BundleContext context) throws Exception {
        this.context = context;
    }

    @Invalidate
    public void stop() {
        for (ServiceRegistration<Template> reg : registrations.values()) {
            try {
                reg.unregister();
            } catch (Exception e) { //NOSONAR
                // Ignore it.
            }
        }
        registrations.clear();
    }

    public void updatedTemplate(File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            LOGGER.info("Thymeleaf template updated for {} ({})", templateFile.getAbsoluteFile(), template.fullName());
            updatedTemplate(template);
        } else {
            try {
                addTemplate(templateFile.toURI().toURL());
            } catch (MalformedURLException e) { //NOSONAR
                // Ignored.
            }
        }
    }

    private ThymeLeafTemplateImplementation getTemplateByFile(File templateFile) {
        try {
            return getTemplateByURL(templateFile.toURI().toURL());
        } catch (MalformedURLException e) {  //NOSONAR
            // Ignored.
        }
        return null;
    }

    private ThymeLeafTemplateImplementation getTemplateByURL(URL url) {
        Collection<ThymeLeafTemplateImplementation> list = registrations.keySet();
        for (ThymeLeafTemplateImplementation template : list) {
            if (template.getURL().sameFile(url)) {
                return template;
            }
        }
        return null;
    }

    public void deleteTemplate(File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            deleteTemplate(template);
        }
    }

    public ThymeLeafTemplateImplementation addTemplate(URL templateURL) {
        ThymeLeafTemplateImplementation template = getTemplateByURL(templateURL);
        if (template != null) {
            // Already existing.
            return template;
        }
        template = new ThymeLeafTemplateImplementation(engine, templateURL, router);
        ServiceRegistration<Template> reg = context.registerService(Template.class, template,
                template.getServiceProperties());
        registrations.put(template, reg);
        LOGGER.info("Thymeleaf template added for {}", templateURL.toExternalForm());
        return template;
    }

    /**
     * Initializes the thymeleaf template engine.
     */
    @Validate
    public void configure() {
        // Thymeleaf specifics
        String mode = configuration.getWithDefault("application.template.thymeleaf.mode", "HTML5");
        int ttl = configuration.getIntegerWithDefault("application.template.thymeleaf.ttl", 1 * 60 * 1000);

        LOGGER.info("Thymeleaf configuration: mode={}, ttl={}", mode, ttl);


        engine = new WisdomTemplateEngine();

        // Initiate the template resolver.
        TemplateResolver resolver = new TemplateResolver();
        resolver.setResourceResolver(new WisdomURLResourceResolver(this));
        resolver.setTemplateMode(mode);
        resolver.setCacheTTLMs((long) ttl);
        engine.setTemplateResolver(resolver);

        engine.setMessageResolver(messageResolver);
        engine.initialize();
    }

    /**
     * Gets the current list of templates.
     *
     * @return the current list of template
     */
    @Override
    public Collection<Template> getTemplates() {
        return new ArrayList<Template>(registrations.keySet());
    }

    @Override
    public String name() {
        return THYMELEAF_ENGINE_NAME;
    }

    @Override
    public String extension() {
        return THYMELEAF_TEMPLATE_EXTENSION;
    }

    public ThymeLeafTemplateImplementation getTemplateByResourceName(String resourceName) {
        Collection<ThymeLeafTemplateImplementation> list = registrations.keySet();
        for (ThymeLeafTemplateImplementation template : list) {
            if (template.fullName().endsWith(resourceName) || template.fullName().endsWith(resourceName + "." + extension())) {
                // TODO Manage duplicates and conflicts
                return template;
            }
            if (template.name().equals(resourceName)) {
                return template;
            }
        }
        return null;
    }

    public void updatedTemplate(ThymeLeafTemplateImplementation template) {
        engine.clearTemplateCacheFor(template.fullName());
    }

    public void deleteTemplate(ThymeLeafTemplateImplementation template) {
        // 1 - unregister the service
        try {
            ServiceRegistration reg = registrations.remove(template);
            if (reg != null) {
                reg.unregister();
            }
        } catch (Exception e) { //NOSONAR
            // May already have been unregistered during the shutdown sequence.
        }

        // 2 - remove the result from the cache
        engine.clearTemplateCacheFor(template.fullName());
    }
}
