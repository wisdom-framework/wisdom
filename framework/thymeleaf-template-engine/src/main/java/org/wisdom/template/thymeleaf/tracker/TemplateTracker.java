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
package org.wisdom.template.thymeleaf.tracker;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.template.thymeleaf.ThymeleafTemplateCollector;
import org.wisdom.template.thymeleaf.impl.ThymeLeafTemplateImplementation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A Chameleon deployer tracking template from bundles.
 */
@Component
@Provides
@Instantiate
public class TemplateTracker implements BundleTrackerCustomizer<List<ThymeLeafTemplateImplementation>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateTracker.class);

    @Requires(proxy = false)
    ThymeleafTemplateCollector engine;

    @Context
    BundleContext context;

    private static final String TEMPLATE_DIRECTORY_IN_BUNDLES = "/templates";

    private BundleTracker<List<ThymeLeafTemplateImplementation>> tracker;

    @Validate
    public void start() {
        LOGGER.debug("Starting Thymeleaf template tracker");
        tracker = new BundleTracker<>(context, Bundle.ACTIVE, this);
        tracker.open();
    }

    @Invalidate
    public void stop() {
        try {
            if (tracker != null) {
                tracker.close();
            }
        } catch (IllegalStateException e) { //NOSONAR
            // We have to catch the exception because of FELIX-4488
        }
    }

    @Override
    public List<ThymeLeafTemplateImplementation> addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        List<ThymeLeafTemplateImplementation> list = new ArrayList<ThymeLeafTemplateImplementation>();
        Enumeration<URL> urls = bundle.findEntries(TEMPLATE_DIRECTORY_IN_BUNDLES, "*.html", true);
        if (urls == null) {
            return list;
        }
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            // Check it's the thymeleaf template.
            if (url.toExternalForm().endsWith(engine.extension())) {
                ThymeLeafTemplateImplementation template = engine.addTemplate(bundle, url);
                list.add(template);
            }
        }
        return list;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, List<ThymeLeafTemplateImplementation> o) {
        for (ThymeLeafTemplateImplementation template : o) {
            engine.updatedTemplate();
        }
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, List<ThymeLeafTemplateImplementation> o) {
        for (ThymeLeafTemplateImplementation template : o) {
            LOGGER.debug("Thymeleaf template deleted for {} from {}", template.fullName(), bundle.getSymbolicName());
            // Check whether we still have an engine.
            if (engine != null) {
                engine.deleteTemplate(template);
            }
        }
    }
}
