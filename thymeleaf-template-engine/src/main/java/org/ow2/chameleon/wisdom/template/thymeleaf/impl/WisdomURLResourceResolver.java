package org.ow2.chameleon.wisdom.template.thymeleaf.impl;

import org.apache.commons.io.FileUtils;
import org.ow2.chameleon.wisdom.template.thymeleaf.TemplateEngine;
import org.ow2.chameleon.wisdom.template.thymeleaf.ThymeLeafTemplateImplementation;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The resource resolver for Thymeleaf.
 * The engine is relying on this class to load the template file.
 */
public class WisdomURLResourceResolver implements IResourceResolver {
    public static final String THYMELEAF_FILE_RESOLVER = "THYMELEAF_FILE_RESOLVER";
    private final TemplateEngine engine;

    public WisdomURLResourceResolver(TemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public String getName() {
        return THYMELEAF_FILE_RESOLVER;
    }

    @Override
    public InputStream getResourceAsStream(TemplateProcessingParameters templateProcessingParameters,
                                           String resourceName) {

        ThymeLeafTemplateImplementation template = engine.getTemplateByResourceName(resourceName);

        if (template == null) {
            LoggerFactory.getLogger(this.getClass()).error("Cannot resolve the template {}, " +
                    "neither {} nor {}.html exist in the template directory or is available in bundles.",
                    resourceName, resourceName, resourceName);
        } else {
            try {
                return template.getURL().openStream();
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).error("Cannot resolve the template {} ({}): cannot open the " +
                        "file.", resourceName, template.getURL().toExternalForm());
            }
        }
        return null;
    }
}
