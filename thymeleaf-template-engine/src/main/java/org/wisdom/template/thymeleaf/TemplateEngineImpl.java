package org.wisdom.template.thymeleaf;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.net.ultraq.thymeleaf.LayoutDialect;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.api.templates.TemplateEngine;
import org.wisdom.template.thymeleaf.dialect.WisdomStandardDialect;
import org.wisdom.template.thymeleaf.impl.ThymeLeafTemplateImplementation;
import org.wisdom.template.thymeleaf.impl.WisdomTemplateEngine;
import org.wisdom.template.thymeleaf.impl.WisdomURLResourceResolver;

/**
 * The main component of the Thymeleaf template engine integration in Wisdom
 */
@Component(immediate = true)
@Provides
@Instantiate(name = "thymeleaf template engine")
public class TemplateEngineImpl implements TemplateEngine {

    @Requires
    private IMessageResolver messageResolver;

    private static String TEMPLATE_DIRECTORY_IN_BUNDLES = "/templates";
    private final BundleContext context;
    @Requires
    private ApplicationConfiguration configuration;

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateEngineImpl.class.getName());
    private File templateDirectory;
    private Map<ThymeLeafTemplateImplementation, ServiceRegistration<Template>> registrations = new ConcurrentHashMap<>();
    private WisdomTemplateEngine engine;
    private FileAlterationMonitor monitor;
    private BundleTracker<List<ThymeLeafTemplateImplementation>> tracker;

    @Requires
    private Router router;

    public TemplateEngineImpl(BundleContext context) throws Exception {
        this.context = context;
        configure();
        initializeDirectoryMonitoring();
        initializeBundleMonitoring();
    }

    private void initializeBundleMonitoring() {
        tracker =
                new BundleTracker<>(context, Bundle.ACTIVE,
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
                                    LOGGER.info("Thymeleaf template deleted for {} from {}", template.fullName(), bundle.getSymbolicName());
                                    // 1 - unregister the service
                                    try {
                                        registrations.get(template).unregister();
                                    } catch (Exception e) { //NOSONAR
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
        FileAlterationObserver observer = new FileAlterationObserver(templateDirectory, FileFilterUtils.or(FileFilterUtils
                .directoryFileFilter(), new SuffixFileFilter("html")));
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                if (file.isDirectory()) {
                    return;
                }
                try {
                    addTemplate(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    LOGGER.error("Cannot compute the url of file {}", file.getAbsolutePath(), e);
                }
            }

            @Override
            public void onFileChange(File file) {
                if (file.isDirectory()) {
                    return;
                }
                updatedTemplate(file);
            }

            @Override
            public void onFileDelete(File file) {
                if (file.isDirectory()) {
                    return;
                }
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
            LOGGER.error("Cannot stop the monitor service gracefully", e);
        }
        for (ServiceRegistration<Template> reg : registrations.values()) {
            try {
                reg.unregister();
            } catch (Exception e) { //NOSONAR
                // Ignore it.
            }
        }
        registrations.clear();
    }

    private void updatedTemplate(File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            LOGGER.info("Thymeleaf template updated for {}", templateFile.getAbsolutePath());
            // remove the result from the cache
            engine.clearTemplateCacheFor(template.fullName());
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

    private void deleteTemplate(File templateFile) {
        ThymeLeafTemplateImplementation template = getTemplateByFile(templateFile);
        if (template != null) {
            LOGGER.info("Thymeleaf template deleted for {}", templateFile.getAbsolutePath());
            // 1 - unregister the service
            try {
                registrations.get(template).unregister();
            } catch (Exception e) { //NOSONAR
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
        LOGGER.info("Thymeleaf template added for {}", templateURL.toExternalForm());
        return template;
    }

    /**
     * Initializes the thymeleaf template engine.
     */
    private void configure() {
        templateDirectory = configuration.getFileWithDefault("application.template.directory", "templates");
        if (!templateDirectory.isDirectory()) {
            LOGGER.info("Creating the template directory : {}", templateDirectory.getAbsolutePath());
            templateDirectory.mkdirs();
        }
        LOGGER.info("Template directory set to {}", templateDirectory.getAbsolutePath());

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
        // TODO Support dynamic extensions ?

        // We clear the dialects as we are using our own standard dialect.
        engine.clearDialects();
        engine.addDialect(new WisdomStandardDialect());
        engine.addDialect(new LayoutDialect());

        LOGGER.info("Thymeleaf Template Engine configured : " + engine);
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
        return "thymeleaf";
    }

    @Override
    public String extension() {
        return "html";
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
