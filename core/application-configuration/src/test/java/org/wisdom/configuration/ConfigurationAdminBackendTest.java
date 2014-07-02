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
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.configuration.ApplicationConfiguration;

import java.io.File;
import java.util.Dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationAdminBackendTest {

    @Test
    public void backendEnabledWithDefaultLocation() {
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration<PersistenceManager> registration = mock(ServiceRegistration.class);
        when(context.registerService(any(Class.class), any(PersistenceManager.class),
                any(Dictionary.class))).thenReturn(registration);
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/junk"));
        when(configuration.getBooleanWithDefault("configadmin.persistence", true)).thenReturn(true);
        when(configuration.getWithDefault("configadmin.storage", ConfigurationAdminBackend.LOCATION)).thenReturn
                (ConfigurationAdminBackend.LOCATION);

        ConfigurationAdminBackend backend = new ConfigurationAdminBackend(context, configuration);
        assertThat(backend.reg).isNotNull();
        assertThat(backend.reg).isEqualTo(registration);
        backend.unregisterQuietly();
    }

    @Test
    public void backendDisabledWithDefaultLocation() {
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration<PersistenceManager> registration = mock(ServiceRegistration.class);
        when(context.registerService(any(Class.class), any(PersistenceManager.class),
                any(Dictionary.class))).thenReturn(registration);
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/junk"));
        when(configuration.getBooleanWithDefault("configadmin.persistence", true)).thenReturn(false);
        when(configuration.getWithDefault("configadmin.storage", ConfigurationAdminBackend.LOCATION)).thenReturn
                (ConfigurationAdminBackend.LOCATION);

        ConfigurationAdminBackend backend = new ConfigurationAdminBackend(context, configuration);
        assertThat(backend.reg).isNull();
        backend.unregisterQuietly();
    }

}