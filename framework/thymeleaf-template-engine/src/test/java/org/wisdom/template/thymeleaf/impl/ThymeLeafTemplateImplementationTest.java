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
package org.wisdom.template.thymeleaf.impl;

import org.junit.Test;
import org.wisdom.template.thymeleaf.impl.ThymeLeafTemplateImplementation;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * checks the Template implementation for Thymeleaf
 *
 * bundle:// cannot be used here as the url handler is not registered. Use file instead.
 */
public class ThymeLeafTemplateImplementationTest {

    @Test
    public void testNameExtractionFromURL() throws MalformedURLException {
        URL url = new URL("file://38.0:0/templates/footer.thl.html");
        ThymeLeafTemplateImplementation template = new ThymeLeafTemplateImplementation(null, url, null, null, null);
        assertThat(template.fullName()).isEqualTo(url.toExternalForm());
        assertThat(template.name()).isEqualTo("footer");
    }

    @Test
    public void testNameExtractionFromURLUsingSubFolder() throws MalformedURLException {
        URL url = new URL("file://38.0:0/templates/hello/footer.thl.html");
        ThymeLeafTemplateImplementation template = new ThymeLeafTemplateImplementation(null, url, null, null, null);
        assertThat(template.fullName()).isEqualTo(url.toExternalForm());
        assertThat(template.name()).isEqualTo("hello/footer");
    }

}
