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
package org.wisdom.validation.hibernate;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.i18n.InternationalizationService;

import javax.validation.MessageInterpolator;
import java.util.Locale;

@Service(ConstraintMessageInterpolator.class)
public class ConstraintMessageInterpolator implements MessageInterpolator {

    private MessageInterpolator defaultInterpolator;

    @Requires(optional = true, nullable = false, proxy = false)
    InternationalizationService i18n;

    @Override
    public String interpolate(String message, Context context) {
        String interpolatedMessage;
        final org.wisdom.api.http.Context ctxt = org.wisdom.api.http.Context.CONTEXT.get();
        if (ctxt != null && ctxt.request().languages().length != 0) {
            interpolatedMessage = interpolate(message, context, ctxt.request().languages()[0]);
        } else {
            interpolatedMessage = interpolate(message, context, Locale.getDefault());
        }
        return interpolatedMessage;
    }

    @Override
    public String interpolate(String s, Context context, Locale locale) {
        String internationalized = null;
        if (i18n != null  && s.startsWith("{")  && s.endsWith("}")) {
            internationalized = i18n.get(locale, s.substring(1, s.length() - 1));
        }
        if (internationalized == null) {
            internationalized = s;
        }
        return defaultInterpolator.interpolate(internationalized, context, locale);
    }

    public void setDefaultInterpolator(MessageInterpolator defaultInterpolator) {
        this.defaultInterpolator = defaultInterpolator;
    }
}
