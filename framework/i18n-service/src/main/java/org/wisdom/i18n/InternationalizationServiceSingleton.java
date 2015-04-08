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

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.*;

/**
 * The default implementation of the internationalization service. It analyses bundles,
 * and loads resource bundles from files contained in the 'i18n' folder of the bundle. The locale is extracted from
 * the file name as follows: name_locale.properties. For example, app.properties is using the default locale,
 * while app_fr is using the French locale. Resource bundles are loaded in UTF-8.
 */
@Component
@Provides(specifications = InternationalizationService.class)
@Instantiate
public class InternationalizationServiceSingleton implements InternationalizationService,
        BundleTrackerCustomizer<List<I18nExtension>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternationalizationService.class);


    private final BundleContext context;
    private final Locale defaultLocale;

    @Requires
    ApplicationConfiguration configuration;

    /**
     * The managed extensions.
     */
    private List<I18nExtension> extensions = new ArrayList<>();
    private BundleTracker<List<I18nExtension>> tracker;

    private Map<Locale, String> etags = new HashMap<>();

    public InternationalizationServiceSingleton(BundleContext context) {
        this.context = context;
        // configuration is null in unit tests (on purpose).
        if (configuration != null) {
            this.defaultLocale = Locale.forLanguageTag(getDefaultLocale());
        } else {
            this.defaultLocale = null;
        }
    }

    protected String getDefaultLocale() {
        return configuration.getWithDefault(APPLICATION_DEFAULT_LOCALE,
            configuration.getWithDefault(APPLICATION_DEFAULT_LOCALE_OLD, Locale.ENGLISH.toLanguageTag()));
    }

    @Validate
    public void start() {
        tracker = new BundleTracker<>(context, Bundle.ACTIVE, this);
        tracker.open();
    }

    @Invalidate
    public void stop() {
        if (tracker != null) {
            tracker.close();
        }
        extensions.clear();
    }

    /**
     * Retrieves the default locale as configured by the application.
     *
     * @return the default locale
     */
    @Override
    public Locale defaultLocale() {
        if (defaultLocale == null) {
            return InternationalizationService.DEFAULT_LOCALE;
        }
        return defaultLocale;
    }

    /**
     * Retrieves the set of resource bundles handled by the system.
     *
     * @return the set of resource bundle, empty if none.
     */
    @Override
    public Collection<ResourceBundle> bundles() {
        Set<ResourceBundle> bundles = new LinkedHashSet<>();
        for (I18nExtension extension : extensions) {
            bundles.add(extension.bundle());
        }
        return bundles;
    }

    /**
     * Retrieves the set of resource bundles handled by the system providing messages for the given locale.
     *
     * @param locale the locale
     * @return the set of resource bundle, empty if none.
     */
    @Override
    public Collection<ResourceBundle> bundles(Locale locale) {
        Set<ResourceBundle> bundles = new LinkedHashSet<>();
        for (I18nExtension extension : extensions) {
            if (extension.locale().equals(locale)) {
                bundles.add(extension.bundle());
            }
        }
        return bundles;
    }


    /**
     * Gets the message identified by `key` for the first locale from the given set of locale that provide a value.
     * The message can be parameterized using  `args`, applied to the message using  {@link java.text.MessageFormat}.
     * If the message is not provided for the given locales, the default locale is tried. If the message is still not
     * provided, {@literal null} is returned.
     *
     * @param locales the ordered set of locales
     * @param key     the key
     * @param args    the arguments (optional)
     * @return the formatted internationalized message
     */
    public String get(Locale[] locales, String key, Object... args) {
        for (Locale locale : locales) {
            I18nExtension extension = getExtension(locale, key);
            if (extension != null) {
                return extension.get(key, args);
            }
        }
        // Use default.
        I18nExtension extension = getExtension(InternationalizationService.DEFAULT_LOCALE, key);
        if (extension != null) {
            return extension.get(key, args);
        }

        return null;
    }

    /**
     * Gets the message identified by `key` for the given locale. The message can be parameterized using `args`,
     * applied to the message using  {@link java.text.MessageFormat}. If the message is not provided for the given
     * locale, the default locale is tried. If the message is still not provided, {@literal null} is returned.
     *
     * @param locale the locale
     * @param key    the key
     * @param args   the arguments (optional)
     * @return the formatted internationalized message
     */
    @Override
    public String get(Locale locale, String key, Object... args) {
        I18nExtension extension = getExtension(locale, key);
        if (extension != null) {
            return extension.get(key, args);
        }
        extension = getExtension(InternationalizationService.DEFAULT_LOCALE, key);
        if (extension != null) {
            return extension.get(key, args);
        }

        return null;
    }

    /**
     * Gets all the messages defined in the given locale AND default locale (for messages not defined in the given
     * locale). The returned map is composed pair of key:message.
     *
     * @param locale the locale
     * @return the set of defined messages.
     */
    public Map<String, String> getAllMessages(Locale locale) {
        return getAllMessages(new Locale[]{locale});
    }

    /**
     * Gets all the messages defined in the given locales AND default locale (for messages not defined in the given
     * any locale). The message are added to the map only if they are not provided in the previous locale,
     * meaning that the order is important. The returned map is composed pair of key:message.
     *
     * @param locales the ordered set of locales
     * @return the set of defined messages.
     */
    @Override
    public Map<String, String> getAllMessages(Locale... locales) {
        Map<String, String> messages = new HashMap<>();
        List<I18nExtension> extensionForLocale;
        for (Locale locale : locales) {
            extensionForLocale = getExtension(locale);
            for (I18nExtension extension : extensionForLocale) {
                merge(messages, extension.map());
            }
        }
        // Now add the messages for the default locale
        extensionForLocale = getExtension(DEFAULT_LOCALE);
        for (I18nExtension extension : extensionForLocale) {
            merge(messages, extension.map());
        }
        return messages;
    }

    /**
     * Retrieves the ETAG for the given locale.
     *
     * @param locale the locale
     * @return the computed etag, must not be {@code null} or empty
     */
    @Override
    public String etag(Locale locale) {
        String etag = etags.get(locale);
        if (etag == null) {
            // We don't have a stored etag, that means we don't have messages. We returns 0.
            // There is a potential race condition here:
            // We retrieve the etag get 0, but when we retrieve the messages, we get messages. The browser receives 0
            // as etag, which will not match the next request. It's should not be too critical as it will just send
            // the same content a second time.
            return "0";
        } else {
            return etag;
        }
    }

    private void merge(Map<String, String> map1, Map<String, String> map2) {
        for (Map.Entry<String, String> entry : map2.entrySet()) {
            if (!map1.containsKey(entry.getKey())) {
                map1.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private List<I18nExtension> getExtension(Locale locale) {
        if (locale.equals(defaultLocale)) {
            locale = InternationalizationService.DEFAULT_LOCALE;
        }
        List<I18nExtension> list = new ArrayList<>();
        for (I18nExtension extension : extensions) {
            if (extension.locale().equals(locale)) {
                list.add(extension);
            } else if (locale.equals(defaultLocale)  && extension.locale().equals(DEFAULT_LOCALE)) {
                list.add(extension);
            }
        }
        return list;
    }

    private I18nExtension getExtension(Locale locale, String key) {
        if (locale.equals(defaultLocale)) {
            locale = InternationalizationService.DEFAULT_LOCALE;
        }
        for (I18nExtension extension : extensions) {
            if (extension.locale().equals(locale)
                    && extension.keys().contains(key)) {
                return extension;
            }
        }
        return null;
    }

    /**
     * A bundle is being added to the {@code BundleTracker}.
     * <p/>
     * <p/>
     * This method is called before a bundle which matched the search parameters
     * of the {@code BundleTracker} is added to the
     * {@code BundleTracker}. This method should return the object to be
     * tracked for the specified {@code Bundle}. The returned object is
     * stored in the {@code BundleTracker} and is available from the
     * {@link org.osgi.util.tracker.BundleTracker#getObject(org.osgi.framework.Bundle) getObject} method.
     *
     * @param bundle The {@code Bundle} being added to the
     *               {@code BundleTracker}.
     * @param event  The bundle event which caused this customizer method to be
     *               called or {@code null} if there is no bundle event associated
     *               with the call to this method.
     * @return The object to be tracked for the specified {@code Bundle}
     * object or {@code null} if the specified {@code Bundle}
     * object should not be tracked.
     */
    @Override
    public List<I18nExtension> addingBundle(Bundle bundle, BundleEvent event) {
        List<I18nExtension> list = ExtenderUtils.analyze("/i18n/", bundle);
        if (list.isEmpty()) {
            return null;
        }
        String current = Long.toString(System.currentTimeMillis());
        LOGGER.info(list.size() + " resource bundle(s) loaded from {} ({})", bundle.getSymbolicName(),
                bundle.getBundleId());
        for (I18nExtension extension : list) {
            extensions.add(extension);
            etags.put(extension.locale(), current);
        }
        return list;
    }


    /**
     * A bundle tracked by the {@code BundleTracker} has been modified.
     * <p/>
     * <p/>
     * This method is called when a bundle being tracked by the
     * {@code BundleTracker} has had its state modified.
     *
     * @param bundle The {@code Bundle} whose state has been modified.
     * @param event  The bundle event which caused this customizer method to be
     *               called or {@code null} if there is no bundle event associated
     *               with the call to this method.
     * @param object The tracked object for the specified bundle.
     */
    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, List<I18nExtension> object) {
        // Not supported.
    }

    /**
     * A bundle tracked by the {@code BundleTracker} has been removed.
     * <p/>
     * <p/>
     * This method is called after a bundle is no longer being tracked by the
     * {@code BundleTracker}.
     *
     * @param bundle The {@code Bundle} that has been removed.
     * @param event  The bundle event which caused this customizer method to be
     *               called or {@code null} if there is no bundle event associated
     *               with the call to this method.
     * @param list   The tracked object for the specified bundle.
     */
    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, List<I18nExtension> list) {
        String current = Long.toString(System.currentTimeMillis());
        for (I18nExtension extension : list) {
            synchronized (this) {
                extensions.remove(extension);
                etags.put(extension.locale(), current);
            }
        }
        LOGGER.info("Bundle {} ({}) does not offer the {} resource bundle(s) anymore",
                bundle.getSymbolicName(), bundle.getBundleId(), list.size());
    }
}
