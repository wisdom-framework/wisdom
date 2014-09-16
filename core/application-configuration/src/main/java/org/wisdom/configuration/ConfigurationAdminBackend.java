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
package org.wisdom.configuration;

import org.apache.felix.cm.PersistenceManager;
import org.apache.felix.cm.file.FilePersistenceManager;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.configuration.ApplicationConfiguration;

import java.io.File;

/**
 * This component provides the Persistence Manager used by Felix Implementation of the Configuration Admin. It set the
 * location where the configurations are stored. If not configured, the configurations are stored into {@literal
 * basedir/conf/configurations}
 */
@Component
@Instantiate
public class ConfigurationAdminBackend {

    /**
     * The location where configurations will be stored.
     */
    public static final String LOCATION = ".config-admin/configurations";

    @Requires
    private ApplicationConfiguration configuration;

    /**
     * The service registration, marked as protected for testing purpose.
     */
    protected ServiceRegistration<PersistenceManager> reg;

    public ConfigurationAdminBackend(@Context BundleContext context,
                                     @Requires ApplicationConfiguration configuration) {
        if (!configuration.getBooleanWithDefault("configadmin.persistence", true)) {
            return;
        }

        String loc = configuration.getWithDefault("configadmin.storage", LOCATION);
        FilePersistenceManager delegate = new FilePersistenceManager(
                new File(configuration.getBaseDir(), loc).getAbsolutePath());
        reg = context.registerService(PersistenceManager.class, delegate, null);
    }

    @Invalidate
    public void unregisterQuietly() {
        if (reg == null) {
            return;
        }
        try {
            reg.unregister();
        } catch (IllegalStateException e) { //NOSONAR
            // Swallow the exception
        }
        reg = null;
    }
}
