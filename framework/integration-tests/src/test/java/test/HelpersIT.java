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
package test;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.test.parents.WisdomTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that integration test can use the OSGi and iPOJO helpers.
 */
public class HelpersIT extends WisdomTest {

    private OSGiHelper osgi;
    private IPOJOHelper ipojo;

    @Before
    public void setUp() {
        osgi = new OSGiHelper(context);
        ipojo = new IPOJOHelper(context);
    }

    @After
    public void tearDown() {
        ipojo.dispose();
        osgi.dispose();
    }

    @Test
    public void testOSGiHelpers() {
        assertThat(osgi.getBundle(2)).isNotNull();
        assertThat(osgi.getServiceObject(ApplicationConfiguration.class)).isNotNull();
    }

    @Test
    public void testiPOJOHelper() {
        Factory factory = ipojo.getFactory("org.wisdom.configuration.ApplicationConfigurationImpl");
        assertThat(factory).isNotNull();
        assertThat(factory.getInstances()).isNotEmpty();
        ComponentInstance instance = factory.getInstances().get(0);
        assertThat(ipojo.getArchitectureByName(instance.getInstanceName())).isNotNull();
    }

}
