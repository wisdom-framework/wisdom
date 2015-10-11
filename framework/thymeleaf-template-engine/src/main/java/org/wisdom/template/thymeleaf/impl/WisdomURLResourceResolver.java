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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.wisdom.api.templates.Template;
import org.wisdom.template.thymeleaf.ThymeleafTemplateCollector;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The resource resolver for Thymeleaf.
 * The engine is relying on this class to load the template file.
 */
public class WisdomURLResourceResolver implements IResourceResolver {
    public static final String THYMELEAF_FILE_RESOLVER = "THYMELEAF_FILE_RESOLVER";
    private final ThymeleafTemplateCollector engine;

    public WisdomURLResourceResolver(ThymeleafTemplateCollector collector) {
        this.engine = collector;
    }

    @Override
    public String getName() {
        return THYMELEAF_FILE_RESOLVER;
    }

    @Override
    public InputStream getResourceAsStream(TemplateProcessingParameters templateProcessingParameters,
                                           String resourceName) {
        // Check whether we have a 'parent' template
        // If so and if the given template name is 'simple' (not absolute), we compute the full path.
        final Template mayBeParentTemplate = (Template) templateProcessingParameters.getContext().getVariables()
                .get("__TEMPLATE__");


        ThymeLeafTemplateImplementation template = engine.getTemplateByResourceName(resourceName);

        if (template == null &&  mayBeParentTemplate != null && !isAbsoluteUrl(resourceName)) {
            // Compute the new name, we retrieve the 'full' path of the parent. It's not the complete URL just the path.
            String path = mayBeParentTemplate.name();
            String absolute = resourceName;
            if (path.contains("/")) {
                // If the path contains a /, remove the part after the / and append the given name
                absolute = path.substring(0, path.lastIndexOf('/')) + "/" + resourceName;
            }
            // Else Nothing do do, we already have the path (root).

            // We normalize to manage the .. case
            absolute = FilenameUtils.normalize(absolute);
            template = engine.getTemplateByResourceName(absolute);
        }

        if (template == null && ! resourceName.startsWith("/")) {
            // Try as absolute
            template = engine.getTemplateByResourceName("/" + resourceName);
        }

        if (template == null) {
            LoggerFactory.getLogger(this.getClass()).error("Cannot resolve the template {}, " +
                            "neither {} nor {}.thl.html exist in the template directory or is available in bundles.",
                    resourceName, resourceName, resourceName);
        } else {
            try {
                return template.getURL().openStream();
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).error("Cannot resolve the template {} ({}): cannot open the " +
                        "file.", resourceName, template.getURL().toExternalForm(), e);
            }
        }
        return null;
    }

    private boolean isAbsoluteUrl(String resourceName) {
        // We identify url using the : character. It's not perfect but should cover most cases.
        return resourceName.contains(":");
    }
}
