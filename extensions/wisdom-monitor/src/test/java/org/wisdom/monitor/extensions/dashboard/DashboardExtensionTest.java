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
package org.wisdom.monitor.extensions.dashboard;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.wisdom.api.configuration.ApplicationConfiguration;

import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DashboardExtensionTest {

    @Test
    public void testStartAndStop() throws Exception {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("monitor.http.enabled", true)).thenReturn(true);
        when(configuration.getBooleanWithDefault("monitor.jmx.enabled", true)).thenReturn(true);

        when(configuration.getIntegerWithDefault("monitor.period", 10)).thenReturn(10);

        when(configuration.getBooleanWithDefault("monitor.graphite.enabled", false)).thenReturn(true);
        when(configuration.getOrDie("monitor.graphite.host")).thenReturn("localhost");
        when(configuration.getOrDie("monitor.graphite.host")).thenReturn("localhost");
        when(configuration.getIntegerOrDie("monitor.graphite.port")).thenReturn(5555);
        // HTTP
        when(configuration.getWithDefault("monitor.http.interception", ".*")).thenReturn(".*");
        when(configuration.getIntegerWithDefault("monitor.http.priority", 10000)).thenReturn(10000);

        BundleContext context = mock(BundleContext.class);

        DashboardExtension extension = new DashboardExtension();
        extension.configuration = configuration;
        extension.bc = context;
        extension.scheduler = Executors.newSingleThreadScheduledExecutor();
        extension.start();

        assertThat(extension.registry.getGauges())
                .containsKeys("jvm.buffers.direct.capacity",
                        "jvm.buffers.direct.count",
                        "jvm.buffers.direct.used",
                        "jvm.threads.waiting.count",
                        "jvm.runtime.uptime"
                        );

        assertThat(extension.registry.getGauges().get("jvm.runtime.uptime")).isNotNull();

        assertThat(extension.registry.counter("http.activeRequests")).isNotNull();
        assertThat(extension.registry.meter("http.responseCodes.others")).isNotNull();
        assertThat(extension.registry.timer("http.requests")).isNotNull();

        extension.stop();
    }

    @Test
    public void testLabel() throws Exception {
        DashboardExtension extension = new DashboardExtension();
        assertThat(extension.label()).isEqualTo("Dashboard");
    }

    @Test
    public void testUrl() throws Exception {
        DashboardExtension extension = new DashboardExtension();
        assertThat(extension.url()).isEqualTo("/monitor/dashboard");
    }

    @Test
    public void testCategory() throws Exception {
        DashboardExtension extension = new DashboardExtension();
        assertThat(extension.category()).isEqualTo("root");
    }
}
