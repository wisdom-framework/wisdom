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
package org.wisdom.i18n;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.*;

/**
 * A controller allowing clients to retrieve the internationalized messages.
 */
@Controller
public class I18nController extends DefaultController {

    @Requires
    InternationalizationService service;

    @Route(method = HttpMethod.GET, uri = "i18n/bundles/{file<.+>}.properties")
    public Result getBundleResource(@PathParameter("file") String file) {

        // Extract the locale from file
        if (Strings.isNullOrEmpty(file)) {
            return notFound().as(MimeTypes.TEXT);
        }

        Locale locale = InternationalizationService.DEFAULT_LOCALE;
        if (file.contains("_")) {
            // We got a locale
            locale = Locale.forLanguageTag(file.substring(file.indexOf('_') + 1).replace("_", "-"));
        }

        Collection<ResourceBundle> bundles = service.bundles(locale);

        // Do we have this locale
        if (bundles.isEmpty()) {
            // No, return not found
            return notFound().as(MimeTypes.TEXT);
        } else {
            StringBuilder builder = new StringBuilder();
            for (ResourceBundle bundle : bundles) {
                for (String key : bundle.keySet()) {
                    builder.append(key).append("=").append(bundle.getString(key)).append("\n");
                }
            }
            return ok(builder.toString()).as(MimeTypes.TEXT);
        }
    }


    @Route(method = HttpMethod.GET, uri = "i18n/{key}")
    public Result getMessage(@Parameter("key") String key, @QueryParameter("locale") Locale locale) {
        String message;
        if (locale != null  && ! locale.equals(InternationalizationService.DEFAULT_LOCALE)) {
            message = service.get(locale, key);
        } else {
            message = service.get(context().request().languages(), key);
        }

        if (message != null) {
            return ok(message).as(MimeTypes.TEXT);
        } else {
            return notFound("No message for " + key).as(MimeTypes.TEXT);
        }
    }

    @Route(method = HttpMethod.GET, uri = "i18n")
    public Result getMessages(@QueryParameter("locale") Locale locale) {
        Map<String, String> messages;
        if (locale != null  && ! locale.equals(InternationalizationService.DEFAULT_LOCALE)) {
            messages = service.getAllMessages(locale);
        } else {
            messages = service.getAllMessages(context().request().languages());
        }

        if (messages != null) {
            return ok(messages).json();
        } else {
            return notFound().json();
        }
    }

}
