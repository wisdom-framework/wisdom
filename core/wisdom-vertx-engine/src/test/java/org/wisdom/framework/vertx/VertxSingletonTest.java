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
package org.wisdom.framework.vertx;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.Vertx;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.test.parents.FakeConfiguration;

import java.util.Dictionary;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class VertxSingletonTest {

    @Test
    public void testNonClusteredEnvironment() throws InterruptedException {
        VertxSingleton singleton = new VertxSingleton();

        singleton.appConfiguration = mock(ApplicationConfiguration.class);
        when(singleton.appConfiguration.getConfiguration("vertx")).thenReturn(new FakeConfiguration(ImmutableMap.of("clustered", false)));


        singleton.context = mock(BundleContext.class);

        singleton.start();
        // Twice, because of the 2 registrations.
        verify(singleton.context, times(2)).registerService(any(Class.class), any(Vertx.class), any(Dictionary.class));
        singleton.stop();
    }

    // Using Surefire - Vertx does not stop Hazelcast properly making a JVM crash on the second start.

    @Test
    @Ignore
    public void testClusteredEnvironmentUsingHostOnly() throws InterruptedException {
        VertxSingleton singleton = new VertxSingleton();

        singleton.appConfiguration = mock(ApplicationConfiguration.class);
        when(singleton.appConfiguration.getConfiguration("vertx")).thenReturn(new FakeConfiguration(ImmutableMap.<String, Object>of(
                "cluster-host", "localhost"
        )));

        singleton.context = mock(BundleContext.class);

        singleton.start();
        // Twice, because of the 2 registrations.
        verify(singleton.context, times(2)).registerService(any(Class.class), any(Vertx.class), any(Dictionary.class));
        singleton.stop();
    }

    @Test
    @Ignore
    public void testClusteredEnvironmentUsingHostAndPort() throws InterruptedException {
        VertxSingleton singleton = new VertxSingleton();

        singleton.appConfiguration = mock(ApplicationConfiguration.class);
        when(singleton.appConfiguration.getConfiguration("vertx")).thenReturn(new FakeConfiguration(ImmutableMap.<String, Object>of(
                "cluster-host", "localhost",
                "cluster-port", 25500
        )));

        singleton.context = mock(BundleContext.class);

        singleton.start();
        // Twice, because of the 2 registrations.
        verify(singleton.context, times(2)).registerService(any(Class.class), any(Vertx.class), any(Dictionary.class));
        singleton.stop();
    }

}