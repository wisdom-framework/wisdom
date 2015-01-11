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
package org.wisdom.framework.csrf;

import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.IProcessor;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.crypto.Hash;
import org.wisdom.api.http.Context;
import org.wisdom.api.templates.TemplateEngine;
import org.wisdom.crypto.CryptoServiceSingleton;
import org.wisdom.framework.csrf.unit.CSRFServiceImplTest;
import org.wisdom.test.parents.FakeContext;

import java.util.Dictionary;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThymeleafCsrfDialectTest {

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
    }

    @Test
    public void testThatServiceIsRegisteredOnTheArrivalOfThymeleaf() {
        ThymeleafCsrfDialect component = new ThymeleafCsrfDialect();
        component.csrf = new CSRFServiceImpl();
        ((CSRFServiceImpl) component.csrf).crypto = new CryptoServiceSingleton(CSRFServiceImplTest.SECRET, Hash.MD5,
                128, Crypto.AES_CBC_ALGORITHM, 20);
        component.context = mock(BundleContext.class);
        when(component.context.registerService(any(Class.class), any(IDialect.class), Matchers.<Dictionary<String, Object>>any()))
                .thenReturn(mock(ServiceRegistration.class));

        assertThat(component.reg).isNull();

        TemplateEngine engine = mock(TemplateEngine.class);
        when(engine.name()).thenReturn("thymeleaf");
        component.bindTemplateEngine(engine);

        assertThat(component.reg).isNotNull();

        component.unbindTemplateEngine(engine);
        assertThat(component.reg).isNull();
    }

    @Test
    public void testTheCreatedProcessorWhenNoTokenCreated() {
        ThymeleafCsrfDialect component = new ThymeleafCsrfDialect();
        component.csrf = new CSRFServiceImpl();
        ((CSRFServiceImpl) component.csrf).crypto = new CryptoServiceSingleton(CSRFServiceImplTest.SECRET, Hash.MD5,
                128, Crypto.AES_CBC_ALGORITHM, 20);
        final Configuration configuration = mock(Configuration.class);
        when(configuration.getWithDefault("token.name", "csrfToken")).thenReturn("csrfToken");
        ((CSRFServiceImpl) component.csrf).configuration = configuration;

        FakeContext ctxt = new FakeContext();
        Context.CONTEXT.set(ctxt);

        IDialect dialect = component.createDialect();
        assertThat(dialect.getPrefix()).isEqualToIgnoringCase("csrf");
        assertThat(dialect.getProcessors()).hasSize(1);

        IProcessor processor = dialect.getProcessors().iterator().next();
        assertThat(processor).isInstanceOf(ThymeleafCsrfDialect.CSRFElementProcessor.class);

        List<Node> nodes = ((ThymeleafCsrfDialect.CSRFElementProcessor) processor).getMarkupSubstitutes(null, null);
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).isInstanceOf(Element.class);
        Element element = (Element) nodes.get(0);
        assertThat(element.getNormalizedName()).isEqualTo("input");
        assertThat(element.getAttributeFromNormalizedName("type").getValue()).isEqualTo("hidden");
        assertThat(element.getAttributeFromNormalizedName("name").getValue()).isEqualTo("csrfToken");
        assertThat(element.getAttributeFromNormalizedName("value").getValue()).isEqualTo("invalid");
    }

    @Test
    public void testTheCreatedProcessorWithToken() {
        ThymeleafCsrfDialect component = new ThymeleafCsrfDialect();
        component.csrf = new CSRFServiceImpl();
        ((CSRFServiceImpl) component.csrf).crypto = new CryptoServiceSingleton(CSRFServiceImplTest.SECRET, Hash.MD5,
                128, Crypto.AES_CBC_ALGORITHM, 20);
        final Configuration configuration = mock(Configuration.class);
        when(configuration.getWithDefault("token.name", "csrfToken")).thenReturn("csrfToken");
        ((CSRFServiceImpl) component.csrf).configuration = configuration;

        FakeContext ctxt = new FakeContext();
        ctxt.getFakeRequest().data().put(CSRFServiceImpl.TOKEN_KEY, "token");
        Context.CONTEXT.set(ctxt);

        IDialect dialect = component.createDialect();
        assertThat(dialect.getPrefix()).isEqualToIgnoringCase("csrf");
        assertThat(dialect.getProcessors()).hasSize(1);

        IProcessor processor = dialect.getProcessors().iterator().next();
        assertThat(processor).isInstanceOf(ThymeleafCsrfDialect.CSRFElementProcessor.class);

        List<Node> nodes = ((ThymeleafCsrfDialect.CSRFElementProcessor) processor).getMarkupSubstitutes(null, null);
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).isInstanceOf(Element.class);
        Element element = (Element) nodes.get(0);
        assertThat(element.getNormalizedName()).isEqualTo("input");
        assertThat(element.getAttributeFromNormalizedName("type").getValue()).isEqualTo("hidden");
        assertThat(element.getAttributeFromNormalizedName("name").getValue()).isEqualTo("csrfToken");
        assertThat(element.getAttributeFromNormalizedName("value").getValue()).isEqualTo("token");
    }

}