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
package org.wisdom.template.thymeleaf;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.thymeleaf.dialect.IDialect;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.templates.Template;
import org.wisdom.template.thymeleaf.impl.MyDialect;
import org.wisdom.template.thymeleaf.impl.WisdomMessageResolver;
import org.wisdom.template.thymeleaf.impl.WisdomTemplateEngine;

import java.io.File;
import java.util.Dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the collector behavior.
 */
public class ThymeleafTemplateCollectorTest {
    @Test
    public void testName() throws Exception {
        BundleContext ctxt = mock(BundleContext.class);
        ThymeleafTemplateCollector collector = new ThymeleafTemplateCollector(ctxt);
        assertThat(collector.name()).isEqualToIgnoringCase("thymeleaf");
    }

    @Test
    public void testExtension() throws Exception {
        BundleContext ctxt = mock(BundleContext.class);
        ThymeleafTemplateCollector collector = new ThymeleafTemplateCollector(ctxt);
        assertThat(collector.extension()).isEqualToIgnoringCase("thl.html");
    }

    @Test
    public void manageTemplates() throws Exception {
        BundleContext ctxt = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(ctxt.getBundle()).thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(ctxt);
        when(ctxt.registerService(any(Class.class), any(Template.class), any(Dictionary.class))).thenReturn(mock
                (ServiceRegistration.class));
        ThymeleafTemplateCollector collector = new ThymeleafTemplateCollector(ctxt);
        collector.configuration = mock(ApplicationConfiguration.class);
        when(collector.configuration.getWithDefault("application.template.thymeleaf.mode",
                "HTML5")).thenReturn("HTML5");
        when(collector.configuration.getIntegerWithDefault("application.template.thymeleaf.ttl",
                60 * 1000)).thenReturn(60 * 1000);
        collector.messageResolver = new WisdomMessageResolver();
        collector.configure();

        assertThat(collector.getTemplates()).isEmpty();
        File javascript = new File("src/test/resources/templates/javascript.thl.html");
        collector.addTemplate(bundle, javascript.toURI().toURL());

        assertThat(collector.getTemplates()).hasSize(1);

        collector.updatedTemplate(bundle, javascript);
        collector.deleteTemplate(javascript);

        assertThat(collector.getTemplates()).hasSize(0);

        collector.stop();
    }

    @Test
    public void testBindAndUnbindDialects() throws Exception {
        BundleContext ctxt = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(ctxt.getBundle()).thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(ctxt);
        when(ctxt.registerService(any(Class.class), any(Template.class), any(Dictionary.class))).thenReturn(mock
                (ServiceRegistration.class));
        ThymeleafTemplateCollector collector = new ThymeleafTemplateCollector(ctxt);
        collector.configuration = mock(ApplicationConfiguration.class);
        when(collector.configuration.getWithDefault("application.template.thymeleaf.mode",
                "HTML5")).thenReturn("HTML5");
        when(collector.configuration.getIntegerWithDefault("application.template.thymeleaf.ttl",
                60 * 1000)).thenReturn(60 * 1000);
        collector.messageResolver = new WisdomMessageResolver();
        collector.configure();

        assertThat(collector.getTemplates()).isEmpty();
        WisdomTemplateEngine engine = collector.engine;
        File javascript = new File("src/test/resources/templates/javascript.thl.html");
        collector.addTemplate(bundle, javascript.toURI().toURL());

        assertThat(collector.getTemplates()).hasSize(1);

        IDialect dialect = new MyDialect();
        collector.bindDialect(dialect);
        assertThat(collector.engine).isNotSameAs(engine);
        engine = collector.engine;
        assertThat(collector.dialects).hasSize(1);

        // Rebind the same.
        collector.bindDialect(dialect);
        assertThat(collector.engine).isSameAs(engine);
        assertThat(collector.dialects).hasSize(1);

        // Unbind
        collector.unbindDialect(dialect);
        assertThat(collector.dialects).hasSize(0);
        assertThat(collector.engine).isNotSameAs(engine);
        engine = collector.engine;

        collector.unbindDialect(dialect);
        assertThat(collector.dialects).hasSize(0);
        assertThat(collector.engine).isSameAs(engine);
    }
}
