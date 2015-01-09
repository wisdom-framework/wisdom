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
package org.wisdom.template.thymeleaf.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.FileResourceResolver;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.util.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyFileResourceResolver implements IResourceResolver {


    private static final Logger logger = LoggerFactory.getLogger(FileResourceResolver.class);

    public static final String NAME = "MY_FILE";


    public MyFileResourceResolver() {
        super();
    }


    public String getName() {
        return NAME;
    }


    public InputStream getResourceAsStream(final TemplateProcessingParameters templateProcessingParameters, String resourceName) {
        Validate.notNull(resourceName, "Resource name cannot be null");
        System.out.println(resourceName);

        if (resourceName.startsWith("file:")) {
            resourceName = resourceName.substring(5);
        }

        final File resourceFile = new File(resourceName);
        try {
            return new FileInputStream(resourceFile);
        } catch (final Exception e) {
            if (logger.isDebugEnabled()) {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            String.format(
                                    "[THYMELEAF][%s][%s] Resource \"%s\" could not be resolved. This can be normal as " +
                                            "maybe this resource is not intended to be resolved by this resolver. " +
                                            "Exception is provided for tracing purposes: ",
                                    TemplateEngine.threadIndex(), templateProcessingParameters.getTemplateName(),
                                    resourceName),
                            e);
                } else {
                    logger.debug(
                            String.format(
                                    "[THYMELEAF][%s][%s] Resource \"%s\" could not be resolved. This can be normal as " +
                                            "maybe this resource is not intended to be resolved by this resolver. " +
                                            "Exception message is provided: %s: %s",
                                    TemplateEngine.threadIndex(), templateProcessingParameters.getTemplateName(),
                                    resourceName, e.getClass().getName(), e.getMessage()));
                }
            }
            return null;
        }
    }

}

