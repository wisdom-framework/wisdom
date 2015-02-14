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

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.i18n.InternationalizationService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * Class responsible of reading the content of a bundle to find the internationalization files.
 */
public final class ExtenderUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternationalizationService.class);
    
    private ExtenderUtils(){
        //Hide implicit constructor
    }

    public static List<I18nExtension> analyze(String path, Bundle bundle) {
        List<I18nExtension> list = new ArrayList<>();
        Enumeration<String> paths = bundle.getEntryPaths(path);
        if (paths != null) {
            while (paths.hasMoreElements()) {
                String entry = paths.nextElement();
                if (! entry.endsWith("/")) {
                    // It's a file, as entries ending with / are directories.
                    String file = entry.substring(path.length() - 1);
                    Locale locale = getLocaleFromResourceName(file);
                    URL url = bundle.getEntry(entry);
                    if (url != null) {
                        I18nExtension extension = new I18nExtension(url, locale, bundle);
                        try {
                            extension.load();
                            list.add(extension);
                        } catch (IOException e) {
                            LOGGER.error("Cannot load resource bundle from " + path + " within " + bundle.getSymbolicName(), e);
                        }
                    } else {
                        LOGGER.error("Cannot open " + entry + " from " + bundle.getSymbolicName());
                    }
                }
            }
        }
        return list;
    }

    public static Locale getLocaleFromResourceName(String name) {
        final int index = name.indexOf('_');
        if (index != -1) {
            String locale = name.substring(index + 1);
            // Remove the extension  (.properties)
            if (locale.lastIndexOf('.') != -1) {
                int lastDot = locale.lastIndexOf('.');
                locale = locale.substring(0, lastDot)
                        // Replace _ (Java syntax) by - to create locale name
                        .replace("_", "-");
            }
            return Locale.forLanguageTag(locale);
        } else {
            return InternationalizationService.DEFAULT_LOCALE;
        }

    }
}
