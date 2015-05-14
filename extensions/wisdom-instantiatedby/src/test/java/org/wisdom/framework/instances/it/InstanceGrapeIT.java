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

import com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.framework.instances.component.grape.C1;
import org.wisdom.framework.instances.component.grape.C2;
import org.wisdom.framework.instances.component.grape.C3;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;


public class InstanceGrapeIT extends WisdomTest {

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
        osgi.dispose();
        ipojo.dispose();
        cleanupConfigurationAdmin();

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
        // Wait until instances are deleted
        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(C1.class) == null
                        && osgi.getServiceObject(C2.class) == null
                        && osgi.getServiceObject(C3.class) == null;

            }
        });
    }

    @Test
    public void testDynamicInstantiationAndDeletion() throws IOException, InterruptedException {
        final Configuration configuration = admin.getConfiguration("org.wisdom.grape");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        properties.put("message", "hello");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return
                        osgi.getServiceObject(C1.class) != null
                                && osgi.getServiceObject(C2.class) != null
                                && osgi.getServiceObject(C3.class) != null;
            }
        });

        final C1 c1 = osgi.getServiceObject(C1.class);
        final C2 c2 = osgi.getServiceObject(C2.class);
        final C3 c3 = osgi.getServiceObject(C3.class);

        assertThat(c1).isNotNull();
        assertThat(c2).isNotNull();
        assertThat(c3).isNotNull();

        assertThat(c1.hello()).contains("wisdom");
        assertThat(c2.hello()).contains("wisdom");
        assertThat(c3.hello()).contains("hello");

        // Deleting the configuration

        configuration.delete();

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(C1.class) == null
                        && osgi.getServiceObject(C2.class) == null
                        && osgi.getServiceObject(C3.class) == null;
            }
        });

        assertThat(osgi.getServiceObject(C1.class)).isNull();
        assertThat(osgi.getServiceObject(C2.class)).isNull();
        assertThat(osgi.getServiceObject(C3.class)).isNull();
    }

    @Test
    public void testDynamicInstantiationAndUpdate() throws IOException, InterruptedException {
        Configuration configuration = admin.getConfiguration("org.wisdom.grape");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        properties.put("message", "hello");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return
                        osgi.getServiceObject(C1.class) != null
                                && osgi.getServiceObject(C2.class) != null
                                && osgi.getServiceObject(C3.class) != null;
            }
        });

        final C1 c1 = osgi.getServiceObject(C1.class);
        final C2 c2 = osgi.getServiceObject(C2.class);
        final C3 c3 = osgi.getServiceObject(C3.class);

        assertThat(c1).isNotNull();
        assertThat(c2).isNotNull();
        assertThat(c3).isNotNull();

        assertThat(c1.hello()).contains("wisdom");
        assertThat(c2.hello()).contains("wisdom");
        assertThat(c3.hello()).contains("hello");

        // Update the configuration
        configuration = admin.getConfiguration("org.wisdom.grape");
        properties = new Properties();
        properties.put("user", "wisdom-2");
        properties.put("message", "hello");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final C1 c1 = osgi.getServiceObject(C1.class);
                final C2 c2 = osgi.getServiceObject(C2.class);
                final C3 c3 = osgi.getServiceObject(C3.class);

                if (c1 != null && c2 != null && c3 != null) {
                    if (c1.hello().contains("wisdom-2")
                            && c2.hello().contains("wisdom-2")
                            && c3.hello().contains("hello")) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Test
    public void testDynamicInstantiationAndDeletionUsingConfigurationFactory() throws IOException,
            InterruptedException {
        final Configuration configuration = admin.createFactoryConfiguration("org.wisdom.grape");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        properties.put("message", "hello");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return
                        osgi.getServiceObject(C1.class) != null
                                && osgi.getServiceObject(C2.class) != null
                                && osgi.getServiceObject(C3.class) != null;
            }
        });

        final C1 c1 = osgi.getServiceObject(C1.class);
        final C2 c2 = osgi.getServiceObject(C2.class);
        final C3 c3 = osgi.getServiceObject(C3.class);

        assertThat(c1).isNotNull();
        assertThat(c2).isNotNull();
        assertThat(c3).isNotNull();

        assertThat(c1.hello()).contains("wisdom");
        assertThat(c2.hello()).contains("wisdom");
        assertThat(c3.hello()).contains("hello");

        // Deleting the configuration

        configuration.delete();

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return osgi.getServiceObject(C1.class) == null
                        && osgi.getServiceObject(C2.class) == null
                        && osgi.getServiceObject(C3.class) == null;
            }
        });

        assertThat(osgi.getServiceObject(C1.class)).isNull();
        assertThat(osgi.getServiceObject(C2.class)).isNull();
        assertThat(osgi.getServiceObject(C3.class)).isNull();
    }

    @Test
    public void testDynamicInstantiationAndUpdateUsingConfigurationFactory() throws IOException, InterruptedException {
        Configuration configuration = admin.createFactoryConfiguration("org.wisdom.grape");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        properties.put("message", "hello");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return
                        osgi.getServiceObject(C1.class) != null
                                && osgi.getServiceObject(C2.class) != null
                                && osgi.getServiceObject(C3.class) != null;
            }
        });

        final C1 c1 = osgi.getServiceObject(C1.class);
        final C2 c2 = osgi.getServiceObject(C2.class);
        final C3 c3 = osgi.getServiceObject(C3.class);

        assertThat(c1).isNotNull();
        assertThat(c2).isNotNull();
        assertThat(c3).isNotNull();

        assertThat(c1.hello()).contains("wisdom");
        assertThat(c2.hello()).contains("wisdom");
        assertThat(c3.hello()).contains("hello");

        // Update the configuration
        configuration = admin.getConfiguration(configuration.getPid());
        properties = new Properties();
        properties.put("user", "wisdom-2");
        properties.put("message", "hello");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final C1 c1 = osgi.getServiceObject(C1.class);
                final C2 c2 = osgi.getServiceObject(C2.class);
                final C3 c3 = osgi.getServiceObject(C3.class);

                if (c1 != null && c2 != null && c3 != null) {
                    if (c1.hello().contains("wisdom-2")
                            && c2.hello().contains("wisdom-2")
                            && c3.hello().contains("hello")) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Test
    public void testDynamicInstantiationAndUpdateUsingConfigurationFactories() throws IOException,
            InterruptedException {
        // First create a configuration
        Configuration configuration = admin.createFactoryConfiguration("org.wisdom.grape");
        Properties properties = new Properties();
        properties.put("user", "wisdom");
        properties.put("message", "hello");
        configuration.update(properties);

        // Check that everyone is there
        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return
                        osgi.getServiceObject(C1.class) != null
                                && osgi.getServiceObject(C2.class) != null
                                && osgi.getServiceObject(C3.class) != null;
            }
        });

        final C1 c1 = osgi.getServiceObject(C1.class);
        final C2 c2 = osgi.getServiceObject(C2.class);
        final C3 c3 = osgi.getServiceObject(C3.class);

        assertThat(c1).isNotNull();
        assertThat(c2).isNotNull();
        assertThat(c3).isNotNull();

        assertThat(c1.hello()).contains("wisdom");
        assertThat(c2.hello()).contains("wisdom");
        assertThat(c3.hello()).contains("hello");

        // Retrieve the instance names. As we have only one instance, retrieve the first name.
        final String name1 = Iterables.get(ipojo.getFactory(C1.class.getName()).getInstancesNames(), 0);
        final String name3 = Iterables.get(ipojo.getFactory(C3.class.getName()).getInstancesNames(), 0);
        final String name2 = Iterables.get(ipojo.getFactory(C2.class.getName()).getInstancesNames(), 0);

        // Create another configuration
        Configuration configuration2 = admin.createFactoryConfiguration("org.wisdom.grape");
        Properties properties2 = new Properties();
        properties2.put("user", "wisdom-2");
        properties2.put("message", "hello-2");
        configuration2.update(properties2);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return
                        osgi.getServiceObjects(C1.class).size() == 2
                                && osgi.getServiceObjects(C2.class).size() == 2
                                && osgi.getServiceObjects(C3.class).size() == 2;
            }
        });


        // Update the configuration
        configuration = admin.getConfiguration(configuration.getPid());
        properties = new Properties();
        properties.put("user", "wisdom-X");
        properties.put("message", "hello");
        configuration.update(properties);

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final C1 c1 = osgi.getServiceObject(C1.class,
                        "(instance.name=" + name1 + ")");
                final C2 c2 = osgi.getServiceObject(C2.class,
                        "(instance.name=" + name2 + ")");
                final C3 c3 = osgi.getServiceObject(C3.class,
                        "(instance.name=" + name3 + ")");

                if (c1 != null && c2 != null && c3 != null) {
                    if (c1.hello().contains("wisdom-X")
                            && c2.hello().contains("wisdom-X")
                            && c3.hello().contains("hello")) {
                        return true;
                    }
                }
                return false;
            }
        });

        // Delete the second configuration
        configuration2.delete();

        await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final C1 c1 = osgi.getServiceObject(C1.class,
                        "(instance.name=" + name1 + ")");
                final C2 c2 = osgi.getServiceObject(C2.class,
                        "(instance.name=" + name2 + ")");
                final C3 c3 = osgi.getServiceObject(C3.class,
                        "(instance.name=" + name3 + ")");
                if (osgi.getServiceObjects(C1.class).size() == 1
                    && osgi.getServiceObjects(C2.class).size() == 1
                    && osgi.getServiceObjects(C3.class).size() == 1) {

                    if (c1 != null && c2 != null && c3 != null) {
                        if (c1.hello().contains("wisdom-X")
                                && c2.hello().contains("wisdom-X")
                                && c3.hello().contains("hello")) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });

    }

}
