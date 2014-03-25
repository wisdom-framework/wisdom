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

import org.wisdom.template.thymeleaf.TemplateEngineImpl;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;

import java.io.IOException;
import java.io.InputStream;

/**
 * The resource resolver for Thymeleaf.
 * The engine is relying on this class to load the template file.
 */
public class WisdomURLResourceResolver implements IResourceResolver {
    public static final String THYMELEAF_FILE_RESOLVER = "THYMELEAF_FILE_RESOLVER";
    private final TemplateEngineImpl engine;

    public WisdomURLResourceResolver(TemplateEngineImpl engine) {
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
                        "file.", resourceName, template.getURL().toExternalForm(), e);
            }
        }
        return null;
    }
}
