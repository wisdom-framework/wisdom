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
package org.wisdom.validation.hibernate;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.content.JacksonModuleRepository;

/**
 * Registers the Jackson Module to serialize the Hibernate's Constraint Violation.
 */
@Component(immediate = true)
@Instantiate
public class HibernateValidatorModule {

    private final Bundle bundle;
    private final SimpleModule module;
    @Requires
    private JacksonModuleRepository repository;

    /**
     * Creates the hibernate validator module .
     * @param context the bundle context.
     */
    public HibernateValidatorModule(BundleContext context) {
        bundle = context.getBundle();
        module = new SimpleModule("Hibernate-Validator-Module", version());
        module.addSerializer(new ConstraintViolationSerializer());
    }

    /**
     * Registers the Jackson module into the repository.
     */
    @Validate
    public void start() {
        repository.register(module);
    }

    /**
     * Unregisters the Jackson module into the repository.
     */
    @Invalidate
    public void stop() {
        repository.unregister(module);
    }

    /**
     * Retrieves the version from the bundle version.
     * @return the version
     */
    public final Version version() {
        return new Version(bundle.getVersion().getMajor(), bundle.getVersion().getMinor(),
                bundle.getVersion().getMicro(), null, null, null);
    }
}
