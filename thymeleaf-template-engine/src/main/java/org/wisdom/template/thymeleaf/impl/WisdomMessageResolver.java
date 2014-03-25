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

import java.util.Locale;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;
import org.wisdom.api.http.Context;
import org.wisdom.api.i18n.InternationalizationService;

/**
 * An implementation of Thymeleaf Message Resolver based on the Wisdom Internationalization Service.
 * @see org.thymeleaf.messageresolver.IMessageResolver
 * @see org.wisdom.api.i18n.InternationalizationService
 */
@Component
@Provides
@Instantiate
public class WisdomMessageResolver extends AbstractMessageResolver implements IMessageResolver {

    @Requires(optional = true)
    InternationalizationService i18n;

    /**
     * <p>
     *   Resolve the message, returning a {@link MessageResolution} object.
     * </p>
     * <p>
     *   If the message cannot be resolved, this method should return null.
     * </p>
     *
     * @param arguments the {@link Arguments} object being used for template processing
     * @param key the message key
     * @param messageParameters the (optional) message parameters
     * @return a {@link MessageResolution} object containing the resolved message,
     * {@literal null} is returned when the resolver cannot retrieve a message for the given key. This policy is
     * compliant with the (Thymeleaf) standard message resolver.
     */
    @Override
    public MessageResolution resolveMessage(Arguments arguments, String key, Object[] messageParameters) {
        Locale[] locales = getLocales();

        String message = i18n.get(locales, key, messageParameters);

        // Same policy as the Thymeleaf standard message resolver.
        if (message == null) {
            return null;
        }

        return new MessageResolution(message);

    }

    private Locale[] getLocales() {
        Context ctx = Context.CONTEXT.get();
        if (ctx == null) {
            return new Locale[]{Locale.getDefault()};
        } else {
            return ctx.request().languages();
        }
    }
}
