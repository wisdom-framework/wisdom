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
package org.wisdom.template.thymeleaf.tracker;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.core.services.Watcher;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.template.thymeleaf.ThymeleafTemplateCollector;

import java.io.File;
import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Check the template deployer behavior.
 */
public class TemplateDeployerTest {

    File directory = new File("target/base");

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(directory);
        directory.mkdirs();
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(directory);
    }


    @Test
    public void start() {
        TemplateDeployer deployer = new TemplateDeployer();
        deployer.watcher = mock(Watcher.class);
        deployer.configuration = mock(ApplicationConfiguration.class);
        when(deployer.configuration.getBaseDir()).thenReturn(directory);
        when(deployer.configuration.getFileWithDefault("application.template.directory",
                "templates")).thenReturn(new File(directory, "templates"));
        deployer.engine = mock(ThymeleafTemplateCollector.class);
        when(deployer.engine.extension()).thenReturn(ThymeleafTemplateCollector.THYMELEAF_TEMPLATE_EXTENSION);

        deployer.start();
        deployer.stop();
    }

    @Test
    public void testAccept() {
        TemplateDeployer deployer = new TemplateDeployer();
        deployer.watcher = mock(Watcher.class);
        deployer.configuration = mock(ApplicationConfiguration.class);
        when(deployer.configuration.getBaseDir()).thenReturn(directory);
        when(deployer.configuration.getFileWithDefault("application.template.directory",
                "templates")).thenReturn(new File(directory, "templates"));
        deployer.engine = mock(ThymeleafTemplateCollector.class);
        when(deployer.engine.extension()).thenReturn(ThymeleafTemplateCollector.THYMELEAF_TEMPLATE_EXTENSION);

        assertThat(deployer.accept(new File("src/test/resources/templates/javascript.thl.html"))).isTrue();
        // no th: in this file:
        assertThat(deployer.accept(new File("src/test/resources/templates/raw.html"))).isFalse();
    }

    @Test
    public void testDynamism() throws MalformedURLException {
        TemplateDeployer deployer = new TemplateDeployer();
        deployer.watcher = mock(Watcher.class);
        deployer.configuration = mock(ApplicationConfiguration.class);
        when(deployer.configuration.getBaseDir()).thenReturn(directory);
        when(deployer.configuration.getFileWithDefault("application.template.directory",
                "templates")).thenReturn(new File(directory, "templates"));
        deployer.engine = mock(ThymeleafTemplateCollector.class);
        when(deployer.engine.extension()).thenReturn(ThymeleafTemplateCollector.THYMELEAF_TEMPLATE_EXTENSION);

        Bundle systemBundle = mock(Bundle.class);
        deployer.context = mock(BundleContext.class);
        when(deployer.context.getBundle(0)).thenReturn(systemBundle);

        File file = new File("src/test/resources/templates/javascript.thl.html");
        deployer.onFileCreate(file);
        verify(deployer.engine).addTemplate(systemBundle, file.toURI().toURL());

        deployer.onFileChange(file);
        verify(deployer.engine).updatedTemplate(systemBundle, file);

        deployer.onFileDelete(file);
        verify(deployer.engine).deleteTemplate(file);
    }
}
