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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.*;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.*;

/**
 * A controller allowing clients to retrieve the internationalized messages.
 */
@Controller
public class I18nController extends DefaultController {

    @Requires
    InternationalizationService service;

    @Requires
    Json json;

    /**
     * Gets the internationalized message as a Java resource bundle (property file).
     *
     * @param file        the name of the file indicating the requested locale
     * @param ifNoneMatch the received ETAG if any
     * @return the result containing the messages as properties.
     */
    @Route(method = HttpMethod.GET, uri = "i18n/bundles/{file<.+>}.properties")
    public Result getBundleResource(@PathParameter("file") String file,
                                    @HttpParameter(HeaderNames.IF_NONE_MATCH) String ifNoneMatch) {

        // Extract the locale from file
        if (Strings.isNullOrEmpty(file)) {
            return notFound().as(MimeTypes.TEXT);
        }

        Locale locale = InternationalizationService.DEFAULT_LOCALE;
        if (file.contains("_")) {
            // We got a locale
            locale = Locale.forLanguageTag(file.substring(file.indexOf('_') + 1).replace("_", "-"));
        }

        // Check if we have received a etag
        String etag = service.etag(locale);
        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return new Result(Status.NOT_MODIFIED);
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
            return ok(builder.toString()).as(MimeTypes.TEXT).with(HeaderNames.ETAG, etag);
        }
    }

    /**
     * Gets the internationalized messages as a JSON document compliant with the I18Next format.
     *
     * @param listOfLocales the list of locales
     * @param ifNoneMatch   the received ETAG if any
     * @return the JSON document containing the messages
     */
    @Route(method = HttpMethod.GET, uri = "i18n/bundles/{file<.+>}.json")
    public Result getBundleResourceForI18Next(@QueryParameter("locales") String listOfLocales,
                                              @HttpParameter(HeaderNames.IF_NONE_MATCH) String ifNoneMatch) {
        // Parse the list of locale
        List<Locale> locales = new ArrayList<>();
        if (!Strings.isNullOrEmpty(listOfLocales)) {
            String[] items = listOfLocales.split(" ");
            for (String item : items) {
                // Manage the 'dev' value (it's the default locale used by i18next
                if ("dev".equalsIgnoreCase(item)) {
                    locales.add(InternationalizationService.DEFAULT_LOCALE);
                } else {
                    locales.add(Locale.forLanguageTag(item));
                }
            }
        }

        String etag;
        StringBuilder builder = new StringBuilder();
        for (Locale locale : locales) {
            builder.append(service.etag(locale));
        }
        etag = builder.toString();

        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return new Result(Status.NOT_MODIFIED);
        }

        // i18next use a specific Json Format
        ObjectNode result = json.newObject();
        for (Locale locale : locales) {
            ObjectNode lang = json.newObject();
            ObjectNode translation = json.newObject();
            lang.set("translation", translation);
            Collection<ResourceBundle> bundles = service.bundles(locale);
            for (ResourceBundle bundle : bundles) {
                for (String key : bundle.keySet()) {
                    populateJsonResourceBundle(translation, key, bundle.getString(key));
                }
            }
            String langName = locale.toLanguageTag();
            if (locale.equals(InternationalizationService.DEFAULT_LOCALE)) {
                langName = "dev";
            }
            result.set(langName, lang);
        }
        return ok(result).with(HeaderNames.ETAG, etag);
    }

    private void populateJsonResourceBundle(ObjectNode node, String key, String value) {
        final int indexOfDot = key.indexOf('.');
        if (indexOfDot != -1) {
            String prefix = key.substring(0, indexOfDot);
            String remainder = key.substring(indexOfDot + 1);
            JsonNode subNode = node.get(prefix);
            if (subNode == null) {
                subNode = json.newObject();
                node.set(prefix, subNode);
            } else if (!subNode.isObject()) {
                throw new IllegalStateException("Invalid JSON Resource Bundle format, the key " + prefix + " is " +
                        "already present and is not an Object Node");
            }
            populateJsonResourceBundle((ObjectNode) subNode, remainder, value);
        } else {
            node.put(key, value);
        }
    }

    /**
     * Gets a specific internationalized message.
     *
     * @param key    the key
     * @param locale the locale. If {@code null} if uses the first locale from the request.
     * @return the message as text, or {@code NOT FOUND} if the key cannot be found.
     */
    @Route(method = HttpMethod.GET, uri = "i18n/{key}")
    public Result getMessage(@Parameter("key") String key, @QueryParameter("locale") Locale locale) {
        String message;
        if (locale != null && !locale.equals(InternationalizationService.DEFAULT_LOCALE)) {
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

    /**
     * Gets all messages of the given locales as JSON. The format is just a set of key:value.
     *
     * @param locales     the locales to include, if {@code null} it uses the languages from the request
     * @param ifNoneMatch the received ETAG if any
     * @return the set of messages
     */
    @Route(method = HttpMethod.GET, uri = "i18n")
    public Result getMessages(@QueryParameter("locales") List<Locale> locales,
                              @HttpParameter(HeaderNames.IF_NONE_MATCH) String ifNoneMatch) {

        // We have to deal with several format here.
        // First, if `locales` is set, use it
        // Finally use the Accept-Language header

        Map<String, String> messages;

        String etag;
        StringBuilder builder = new StringBuilder();
        if (locales != null && !locales.isEmpty()) {
            for (Locale locale : locales) {
                builder.append(service.etag(locale));
            }
        } else {
            for (Locale locale : context().request().languages()) {
                builder.append(service.etag(locale));
            }
        }
        etag = builder.toString();

        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return new Result(Status.NOT_MODIFIED);
        }

        if (locales != null && !locales.isEmpty()) {
            messages = service.getAllMessages(locales.toArray(new Locale[locales.size()]));
        } else {
            messages = service.getAllMessages(context().request().languages());
        }

        if (messages != null) {
            return ok(messages).with(HeaderNames.ETAG, etag).json();
        } else {
            return notFound().json();
        }
    }

}
