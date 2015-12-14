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

import ognl.OgnlRuntime;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.api.templates.TemplateEngine;
import org.wisdom.template.thymeleaf.impl.ThymeLeafTemplateImplementation;
import org.wisdom.template.thymeleaf.impl.WisdomTemplateEngine;
import org.wisdom.template.thymeleaf.impl.WisdomURLResourceResolver;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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

    /**
     * The internal engine. Accesses need to be synchronized as we change the engine instance when
     * dialects arrive and leave.
     */
    WisdomTemplateEngine engine;

    @Requires
    private Router router;

    @Requires(optional = true)
    private Assets assets;

    Set<IDialect> dialects = new HashSet<>();


    /**
     * Creates the collector.
     *
     * @param context the bundle context. This bundle context is used to registers the {@link org.wisdom.api
     *                .templates.Template} services.
     */
    public ThymeleafTemplateCollector(BundleContext context) {
        this.context = context;
    }

    /**
     * Stops the collector. This methods clear all registered {@link org.wisdom.api.templates.Template} services.
     */
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

    /**
     * Updates the template object using the given file as backend.
     *
     * @param bundle the bundle containing the template, use system bundle for external templates.
     * @param templateFile the template file
     */
    public void updatedTemplate(Bundle bundle, File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            LOGGER.debug("Thymeleaf template updated for {} ({})", templateFile.getAbsoluteFile(), template.fullName());
            updatedTemplate();
        } else {
            try {
                addTemplate(bundle, templateFile.toURI().toURL());
            } catch (MalformedURLException e) { //NOSONAR
                // Ignored.
            }
        }
    }

    /**
     * Gets the template object using the given file as backend.
     *
     * @param templateFile the file
     * @return the template object, {@literal null} if not found
     */
    private ThymeLeafTemplateImplementation getTemplateByFile(File templateFile) {
        try {
            return getTemplateByURL(templateFile.toURI().toURL());
        } catch (MalformedURLException e) {  //NOSONAR
            // Ignored.
        }
        return null;
    }

    /**
     * Gets the template object using the given url as backend.
     *
     * @param url the url
     * @return the template object, {@literal null} if not found
     */
    private ThymeLeafTemplateImplementation getTemplateByURL(URL url) {
        Collection<ThymeLeafTemplateImplementation> list = registrations.keySet();
        for (ThymeLeafTemplateImplementation template : list) {
            if (template.getURL().sameFile(url)) {
                return template;
            }
        }
        return null;
    }

    /**
     * Deletes the template using the given file as backend.
     *
     * @param templateFile the file
     */
    public void deleteTemplate(File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            deleteTemplate(template);
        }
    }

    /**
     * Adds a template form the given url.
     *
     * @param bundle the bundle containing the template, use system bundle for external templates.
     * @param templateURL the url
     * @return the added template. IF the given url is already used by another template, return this other template.
     */
    public ThymeLeafTemplateImplementation addTemplate(Bundle bundle, URL templateURL) {
        ThymeLeafTemplateImplementation template = getTemplateByURL(templateURL);
        if (template != null) {
            // Already existing.
            return template;
        }
        synchronized (this) {
            // need to be synchronized because of the access to engine.
            template = new ThymeLeafTemplateImplementation(engine, templateURL,
                    router, assets, bundle);
        }
        ServiceRegistration<Template> reg = context.registerService(Template.class, template,
                template.getServiceProperties());
        registrations.put(template, reg);
        LOGGER.debug("Thymeleaf template added for {}", templateURL.toExternalForm());
        return template;
    }

    /**
     * Initializes the thymeleaf template engine.
     */
    @Validate
    public synchronized void configure() {
        // Thymeleaf specifics
        String mode = configuration.getWithDefault("application.template.thymeleaf.mode", "HTML5");

        int ttl = configuration.getIntegerWithDefault("application.template.thymeleaf.ttl", 60 * 1000);
        if (configuration.isDev()) {
            // In dev mode, reduce the ttl to the strict minimum so we are sure to have updated template rendering.
            ttl = 1;
        }


        LOGGER.debug("Thymeleaf configuration: mode={}, ttl={}", mode, ttl);

        // A TCCL switch is required here as the default Thymeleaf engine initialization triggers a class loading
        // from a class that may be present in the class path  (org/apache/xerces/xni/parser/XMLParserConfiguration).
        // By setting the TCCL, it fails quietly, if not, it may find it but failed to instantiate it (version
        // mismatch or whatever). As this class is only used to  support the HTML5LEGACY Templates (so not use here),
        // we don't really care.

        final ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            engine = new WisdomTemplateEngine(dialects);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }

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
     * A new dialect is now available.
     * @param dialect the dialect
     */
    @Bind(optional = true, aggregate = true)
    public synchronized void bindDialect(IDialect dialect) {
        LOGGER.debug("Binding a new dialect using the prefix '{}' and containing {}",
                dialect.getPrefix(),
                dialect.getProcessors());
        if (this.dialects.add(dialect)) {
            // We must reconfigure the engine
            configure();
            // Update all templates.
            for (Template template : getTemplates()) {
                ((ThymeLeafTemplateImplementation) template).updateEngine(engine);
            }
        }
    }

    /**
     * A dialect has left.
     * @param dialect the dialect that has left
     */
    @Unbind
    public synchronized void unbindDialect(IDialect dialect) {
        LOGGER.debug("Binding a new dialect {}, processors: {}", dialect.getPrefix(),
                dialect.getProcessors());
        if (this.dialects.remove(dialect)) {
            configure();
            for (Template template : getTemplates()) {
                ((ThymeLeafTemplateImplementation) template).updateEngine(engine);
            }
        }
    }

    /**
     * Gets the current list of templates.
     *
     * @return the current list of template
     */
    @Override
    public Collection<Template> getTemplates() {
        return new ArrayList<>(registrations.keySet());
    }

    /**
     * @return {@link #THYMELEAF_ENGINE_NAME}.
     */
    @Override
    public String name() {
        return THYMELEAF_ENGINE_NAME;
    }

    /**
     * @return {@link #THYMELEAF_TEMPLATE_EXTENSION}.
     */
    @Override
    public String extension() {
        return THYMELEAF_TEMPLATE_EXTENSION;
    }

    /**
     * Finds a template object from the given resource name. The first template matching the given name is returned.
     *
     * @param resourceName the name
     * @return the template object.
     */
    public ThymeLeafTemplateImplementation getTemplateByResourceName(String resourceName) {
        Collection<ThymeLeafTemplateImplementation> list = registrations.keySet();
        for (ThymeLeafTemplateImplementation template : list) {
            if (template.fullName().endsWith(resourceName)
                    || template.fullName().endsWith(resourceName + "." + extension())) {
                return template;
            }
            if (template.name().equals(resourceName)) {
                return template;
            }
        }
        return null;
    }

    /**
     * Clears the cache when a template have been updated.
     */
    public synchronized void updatedTemplate() {
        // Synchronized because of the access to engine.
        engine.getCacheManager().clearAllCaches();
    }

    /**
     * Deletes the given template. The service is unregistered, and the cache is cleared.
     *
     * @param template the template
     */
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

        // 2 - as templates can have dependencies, and expressions kept in memory, we clear all caches.
        // Despite this may really impact performance, it should not happen too often on real systems.
        synchronized (this) {
            engine.getCacheManager().clearAllCaches();
        }
        OgnlRuntime.clearCache();
        // Unfortunately, the previous method do not clear the get and set method cache
        // (ognl.OgnlRuntime.cacheGetMethod and ognl.OgnlRuntime.cacheSetMethod)
        clearMethodCaches();
    }

    private void clearMethodCaches() {
        try {
            final Field cacheGetMethod = OgnlRuntime.class.getDeclaredField("cacheGetMethod");
            final Field cacheSetMethod = OgnlRuntime.class.getDeclaredField("cacheSetMethod");
            if (! cacheGetMethod.isAccessible()) {
                cacheGetMethod.setAccessible(true);
            }
            if (! cacheSetMethod.isAccessible()) {
                cacheSetMethod.setAccessible(true);
            }
            ((Map) cacheGetMethod.get(null)).clear();
            ((Map) cacheSetMethod.get(null)).clear();
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
            LOGGER.error("Cannot clean Thymeleaf cache, an exception has been thrown while clearing the Method " +
                    "caches, this may introduce leaks", e);
        }
    }
}
