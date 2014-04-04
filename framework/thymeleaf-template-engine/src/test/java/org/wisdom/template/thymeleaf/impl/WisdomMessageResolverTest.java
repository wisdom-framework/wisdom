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
import org.thymeleaf.messageresolver.MessageResolution;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the behavior of the Message Resolver
 */
public class WisdomMessageResolverTest {

    private static Locale[] locales = new Locale[] { Locale.getDefault() };

    private static Object[] NULL_ARRAY = null;

    @Test
    public void testMessageWhenNoI18N() {
        WisdomMessageResolver resolver = new WisdomMessageResolver();
        resolver.i18n = mock(InternationalizationService.class);
        MessageResolution resolution = resolver.resolveMessage(null, "welcome", null);
        assertThat(resolution).isNull();
    }

    @Test
    public void testMessageWithAI18NButNoMessage() {
        WisdomMessageResolver resolver = new WisdomMessageResolver();
        resolver.i18n = mock(InternationalizationService.class);
        when(resolver.i18n.get(locales, "welcome", NULL_ARRAY)).thenReturn("welcome");

        MessageResolution resolution = resolver.resolveMessage(null, "welcome", null);
        assertThat(resolution).isNotNull();
        assertThat(resolution.getResolvedMessage()).isEqualTo("welcome");

        resolution = resolver.resolveMessage(null, "missing", null);
        assertThat(resolution).isNull();
    }

    @Test
    public void testMessageWithParameters() {
        WisdomMessageResolver resolver = new WisdomMessageResolver();
        resolver.i18n = mock(InternationalizationService.class);
        when(resolver.i18n.get(locales, "welcome", "wisdom")).thenReturn("welcome wisdom");
        when(resolver.i18n.get(locales, "welcome", NULL_ARRAY)).thenReturn("welcome");

        MessageResolution resolution = resolver.resolveMessage(null, "welcome", null);
        assertThat(resolution).isNotNull();
        assertThat(resolution.getResolvedMessage()).isEqualTo("welcome");

        resolution = resolver.resolveMessage(null, "welcome", new Object[] {"wisdom"});
        assertThat(resolution).isNotNull();
        assertThat(resolution.getResolvedMessage()).isEqualTo("welcome wisdom");
    }

}
