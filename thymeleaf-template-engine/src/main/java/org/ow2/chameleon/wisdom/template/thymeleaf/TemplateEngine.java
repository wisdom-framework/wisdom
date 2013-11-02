package org.ow2.chameleon.wisdom.template.thymeleaf;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;
import org.ow2.chameleon.wisdom.template.thymeleaf.dialect.WisdomStandardDialect;
import org.ow2.chameleon.wisdom.template.thymeleaf.impl.WisdomResolver;
import org.ow2.chameleon.wisdom.template.thymeleaf.impl.WisdomTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main component of the Thymeleaf template engine integration in Wisdom
 */
@Component(immediate = true)
@Provides
@Instantiate(name = "thymeleaf template engine")
public class TemplateEngine implements org.ow2.chameleon.wisdom.api.templates.TemplateEngine {

    //TODO Support link, reverse routing, messages...

    private static String TEMPLATE_DIRECTORY_IN_BUNDLES = "/templates";
    private final BundleContext context;
    @Requires
    private ApplicationConfiguration configuration;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private File templateDirectory;
    private ConcurrentHashMap<ThymeLeafTemplateImplementation, ServiceRegistration<Template>> registrations = new ConcurrentHashMap<>();
    private WisdomTemplateEngine engine;
    private FileAlterationMonitor monitor;
    private BundleTracker<List<ThymeLeafTemplateImplementation>> tracker;

    @Requires
    private Router router;

    public TemplateEngine(BundleContext context) throws Exception {
        this.context = context;
        configure();
        initializeDirectoryMonitoring();
        initializeBundleMonitoring();
    }

    private void initializeBundleMonitoring() {
        tracker =
                new BundleTracker<List<ThymeLeafTemplateImplementation>>(context, Bundle.ACTIVE,
                        new BundleTrackerCustomizer<List<ThymeLeafTemplateImplementation>>() {
                            @Override
                            public List<ThymeLeafTemplateImplementation> addingBundle(Bundle bundle, BundleEvent bundleEvent) {
                                List<ThymeLeafTemplateImplementation> list = new ArrayList<>();
                                Enumeration<URL> urls = bundle.findEntries(TEMPLATE_DIRECTORY_IN_BUNDLES, "*.html", true);
                                if (urls == null) {
                                    return list;
                                }
                                while (urls.hasMoreElements()) {
                                    URL url = urls.nextElement();
                                    ThymeLeafTemplateImplementation template = addTemplate(url);
                                    list.add(template);
                                }
                                return list;
                            }

                            @Override
                            public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, List<ThymeLeafTemplateImplementation> o) {
                                for (ThymeLeafTemplateImplementation template : o) {
                                    engine.clearTemplateCacheFor(template.fullName());
                                }
                            }

                            @Override
                            public void removedBundle(Bundle bundle, BundleEvent bundleEvent, List<ThymeLeafTemplateImplementation> o) {
                                for (ThymeLeafTemplateImplementation template : o) {
                                    logger.info("Thymeleaf template deleted for {} from {}", template.fullName(), bundle.getSymbolicName());
                                    // 1 - unregister the service
                                    try {
                                        registrations.get(template).unregister();
                                    } catch (Exception e) {
                                        // May already have been unregistered during the shutdown sequence.
                                    }

                                    // 2 - remove the result from the cache
                                    engine.clearTemplateCacheFor(template.fullName());
                                }
                            }

                        });

        tracker.open();
    }

    private void initializeDirectoryMonitoring() throws Exception {
        monitor = new FileAlterationMonitor(2000);
        FileAlterationObserver observer = new FileAlterationObserver(templateDirectory, new SuffixFileFilter(".html"));
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                try {
                    addTemplate(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.error("Cannot compute the url of file {}", file.getAbsolutePath());
                }
            }

            @Override
            public void onFileChange(File file) {
                updatedTemplate(file);
            }

            @Override
            public void onFileDelete(File file) {
                deleteTemplate(file);
            }
        });
        monitor.addObserver(observer);
        monitor.start();

        Collection<File> files = FileUtils.listFiles(templateDirectory, new String[]{"html"}, true);
        for (File file : files) {
            addTemplate(file.toURI().toURL());
        }

    }

    @Invalidate
    public void stop() {
        tracker.close();
        try {
            monitor.stop();
        } catch (Exception e) {
            logger.error("Cannot stop the monitor service seamlessly", e);
        }
        for (ServiceRegistration<Template> reg : registrations.values()) {
            try {
                reg.unregister();
            } catch (Exception e) {
                // Ignore it.
            }
        }
        registrations.clear();
    }

    private void updatedTemplate(File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            logger.info("Thymeleaf template updated for {}", templateFile.getAbsolutePath());
            // remove the result from the cache
            engine.clearTemplateCacheFor(template.fullName());
        } else {
            try {
                addTemplate(templateFile.toURI().toURL());
            } catch (MalformedURLException e) {
                // Ignored.
            }
        }
    }

    private ThymeLeafTemplateImplementation getTemplateByFile(File templateFile) {
        try {
            return getTemplateByURL(templateFile.toURI().toURL());
        } catch (MalformedURLException e) {
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

    private void deleteTemplate(File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            logger.info("Thymeleaf template deleted for {}", templateFile.getAbsolutePath());
            // 1 - unregister the service
            try {
                registrations.get(template).unregister();
            } catch (Exception e) {
                // May already have been unregistered during the shutdown sequence.
            }

            // 2 - remove the result from the cache
            engine.clearTemplateCacheFor(template.fullName());
        }
    }

    private ThymeLeafTemplateImplementation addTemplate(URL templateURL) {
        ThymeLeafTemplateImplementation template = getTemplateByURL(templateURL);
        if (template != null) {
            // Already existing.
            return template;
        }
        template = new ThymeLeafTemplateImplementation(engine, templateURL, router);
        ServiceRegistration<Template> reg = context.registerService(Template.class, template,
                template.getServiceProperties());
        registrations.put(template, reg);
        logger.info("Thymeleaf template added for {}", templateURL.toExternalForm());
        return template;
    }

    /**
     * Initializes the thymeleaf template engine.
     */
    private void configure() {
        templateDirectory = configuration.getFileWithDefault("application.template.directory", "templates");
        if (!templateDirectory.isDirectory()) {
            logger.info("Creating the template directory : {}", templateDirectory.getAbsolutePath());
            templateDirectory.mkdirs();
        }
        logger.info("Template directory set to {}", templateDirectory.getAbsolutePath());

        // Thymeleaf specifics
        String mode = configuration.getWithDefault("application.template.thymeleaf.mode", "HTML5");
        int ttl = configuration.getIntegerWithDefault("application.template.thymeleaf.ttl", 1 * 60 * 1000);

        logger.info("Thymeleaf configuration: mode={}, ttl={}", mode, ttl);

        WisdomResolver resolver = new WisdomResolver(this);
        resolver.setTemplateMode(mode);
        resolver.setCacheTTLMs((long) ttl);

        engine = new WisdomTemplateEngine();
        engine.setTemplateResolver(resolver);
        // TODO Support dynamic extensions ?
        // TODO Support message.

        // We clear the dialects as we are using our own standard dialect.
        engine.clearDialects();
        engine.addDialect(new WisdomStandardDialect());
        engine.addDialect(new LayoutDialect());


        logger.info("Thymeleaf Template Engine configured : " + engine);
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

    public ThymeLeafTemplateImplementation getTemplateByResourceName(String resourceName) {
        Collection<ThymeLeafTemplateImplementation> list = registrations.keySet();
        for (ThymeLeafTemplateImplementation template : list) {
            if (template.fullName().endsWith(resourceName) || template.fullName().endsWith(resourceName + ".html")) {
                // TODO Manage duplicates and conflicts
                return template;
            }
            if (template.name().equals(resourceName)) {
                return template;
            }
        }
        return null;
    }
}
