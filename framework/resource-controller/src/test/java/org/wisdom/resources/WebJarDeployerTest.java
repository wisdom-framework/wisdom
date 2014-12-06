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
package org.wisdom.resources;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.core.services.Deployer;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebJarDeployerTest {


    private File dir;

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(dir);
    }

    @Test
    public void testStartStop() {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        ServiceRegistration<Deployer> reg = mock(ServiceRegistration.class);
        dir = new File("target/junk/webjars");
        when(bundle.getDataFile("webjars")).thenReturn(dir);
        when(context.getBundle()).thenReturn(bundle);
        WebJarController controller = mock(WebJarController.class);
        WebJarDeployer deployer = new WebJarDeployer(context, controller);
        when(context.registerService(Deployer.class, deployer, null)).thenReturn(reg);

        deployer.start();
        deployer.stop();
    }

    @Test
    public void testDeploy() {
        File acorn = new File("src/test/resources/webjars/acorn-0.5.0.jar");
        assertThat(acorn).isFile();
        assertThat(WebJarDeployer.isWebJar(acorn)).isNotEmpty().hasSize(1);
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(bundle.getDataFile("webjars")).thenReturn(new File("target/junk/webjars"));
        when(context.getBundle()).thenReturn(bundle);
        WebJarController controller = mock(WebJarController.class);
        WebJarDeployer deployer = new WebJarDeployer(context, controller);

        deployer.onFileCreate(acorn);

        // acorn should have been expanded
        File expanded = new File("target/junk/webjars/acorn-0.5.0/acorn/0.5.0");
        assertThat(expanded).isDirectory();

        deployer.onFileChange(acorn);

        // Undeploy it.
        deployer.onFileDelete(acorn);
        assertThat(expanded).doesNotExist();
    }

}