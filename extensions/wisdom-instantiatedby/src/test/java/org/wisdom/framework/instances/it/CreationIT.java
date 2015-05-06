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
package org.wisdom.framework.instances.it;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.framework.instances.component.MyComponent;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;


public class CreationIT extends WisdomTest {

    @Inject
    ConfigurationAdmin admin;

    @Inject
    BundleContext context;
    private OSGiHelper osgi;
    private IPOJOHelper ipojo;

    @Before
    public void init() throws IOException, InvalidSyntaxException {
        osgi = new OSGiHelper(context);
        ipojo = new IPOJOHelper(context);
        cleanupConfigurationAdmin();
    }

    @After
    public void shutdown() throws IOException, InvalidSyntaxException {
        cleanupConfigurationAdmin();
        osgi.dispose();
        ipojo.dispose();

    }

    protected void cleanupConfigurationAdmin() throws IOException, InvalidSyntaxException {
        Configuration[] configurations = admin.listConfigurations(null);
        if (configurations != null) {
            for (Configuration conf : configurations) {
                if (!conf.getPid().contains("instantiated.at.boot")) {
                    conf.delete();
                }
            }
        }

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(MyComponent.class) == null;
            }
        });
    }

    @Test
    public void testDynamicInstantiationAndDeletion() throws IOException, InterruptedException {
        assertThat(osgi.getServiceObject(MyComponent.class)).isNull();

        final Configuration configuration = admin.getConfiguration("org.wisdom.conf");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(MyComponent.class) != null;
            }
        });

        MyComponent service = osgi.getServiceObject(MyComponent.class);
        assertThat(service).isNotNull();
        assertThat(service.hello()).contains("wisdom");

        // Deleting the configuration

        configuration.delete();

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(MyComponent.class) == null;
            }
        });

        assertThat(osgi.getServiceObject(MyComponent.class)).isNull();
    }

    @Test
    public void testDynamicInstantiationAndUpdate() throws IOException, InterruptedException {
        assertThat(osgi.getServiceObject(MyComponent.class)).isNull();

        Configuration configuration = admin.getConfiguration("org.wisdom.conf");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(MyComponent.class) != null;
            }
        });

        MyComponent service = osgi.getServiceObject(MyComponent.class);
        assertThat(service).isNotNull();
        assertThat(service.hello()).contains("wisdom");

        // Update the configuration
        configuration = admin.getConfiguration("org.wisdom.conf");
        properties = new Properties();
        properties.put("user", "wisdom-2");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                MyComponent cmp = osgi.getServiceObject(MyComponent.class);
                if (cmp != null) {
                    if (cmp.hello().contains("wisdom-2")) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Test
    public void testDynamicInstantiationAndDeletionUsingConfigurationFactory()
            throws IOException, InterruptedException {
        assertThat(osgi.getServiceObject(MyComponent.class)).isNull();

        final Configuration configuration = admin.createFactoryConfiguration("org.wisdom.conf");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(MyComponent.class) != null;
            }
        });

        MyComponent service = osgi.getServiceObject(MyComponent.class);
        assertThat(service).isNotNull();
        assertThat(service.hello()).contains("wisdom");

        // Deleting the configuration

        configuration.delete();

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(MyComponent.class) == null;
            }
        });

        assertThat(osgi.getServiceObject(MyComponent.class)).isNull();
    }

    @Test
    public void testDynamicInstantiationAndUpdateUsingConfigurationFactory() throws IOException, InterruptedException {
        assertThat(osgi.getServiceObject(MyComponent.class)).isNull();

        Configuration configuration = admin.createFactoryConfiguration("org.wisdom.conf");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(MyComponent.class) != null;
            }
        });

        MyComponent service = osgi.getServiceObject(MyComponent.class);
        assertThat(service).isNotNull();
        assertThat(service.hello()).contains("wisdom");

        // Update the configuration
        configuration = admin.getConfiguration(configuration.getPid());
        properties = new Properties();
        properties.put("user", "wisdom-2");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                MyComponent cmp = osgi.getServiceObject(MyComponent.class);
                if (cmp != null) {
                    if (cmp.hello().contains("wisdom-2")) {
                        return true;
                    }
                }
                return false;
            }
        });
    }


}
