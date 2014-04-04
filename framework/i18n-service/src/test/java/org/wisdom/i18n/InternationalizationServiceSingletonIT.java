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

import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.i18n.InternationalizationService;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

/**
 * Check the behavior of the internationalization service.
 */
public class InternationalizationServiceSingletonIT extends WisdomTest {

    @Inject
    private InternationalizationService service;

    @Inject
    private BundleContext context;

    private OSGiHelper osgi;
    private List<Bundle> bundles;

    @Before
    public void setUp() {
        osgi = new OSGiHelper(context);
        bundles = new ArrayList<>();
    }

    @After
    public void tearDown() {
        for(Bundle bundle : bundles) {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                // Ignore it
            }
        }
        try {
            osgi.dispose();
        } catch (Exception e) {
            // Ignore it
        }
    }


    /**
     * The test bundle contains the src/test/resources/i18n folder, so contains 2 resource bundles.
     */
    @Test
    public void testDetectionOfFilesFromTheTestBundle() {
        assertThat(service).isNotNull();

        Map<String, String> messages = service.getAllMessages(Locale.FRENCH);
        assertThat(messages).contains(
                MapEntry.entry("welcome", "bonjour"),
                MapEntry.entry("lang", "français"),
                MapEntry.entry("autre", "autre"),
                MapEntry.entry("extra", "extra")
        );

        messages = service.getAllMessages(Locale.CHINA);
        assertThat(messages).contains(
                MapEntry.entry("welcome", "hello"),
                MapEntry.entry("lang", "english"),
                MapEntry.entry("extra", "extra")
        );
        assertThat(messages).doesNotContainKey("autre");
    }

    @Test
    public void testManagementOfABundleContainingInternationalizationFiles() throws FileNotFoundException, BundleException {
        Bundle bundle = osgi.installAndStart("local:/i18n",
                bundle()
                .add("i18n/app.properties", new FileInputStream("src/test/resources/integration/app.properties"))
                .add("i18n/app_fr.properties", new FileInputStream("src/test/resources/integration/app_fr.properties"))
                .build());
        bundles.add(bundle);

        Map<String, String> messages = service.getAllMessages(Locale.FRENCH);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "mon application")
        );

        messages = service.getAllMessages(Locale.CHINA);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "my application")
        );

        messages = service.getAllMessages(Locale.ENGLISH);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "my application")
        );

        messages = service.getAllMessages(Locale.GERMAN);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "my application")
        );

        bundle.stop();

        messages = service.getAllMessages(Locale.FRENCH);
        assertThat(messages).doesNotContainKey("app.name");
        messages = service.getAllMessages(Locale.GERMAN);
        assertThat(messages).doesNotContainKey("app.name");
    }

    /**
     * In this test, a first bundle provides the default and french resources, while another one provides the german
     * version.
     */
    @Test
    public void testWithTwoBundles() throws FileNotFoundException, BundleException {
        Bundle bundle1 = osgi.installAndStart("local:/i18n",
                bundle()
                        .add("i18n/app.properties", new FileInputStream("src/test/resources/integration/app.properties"))
                        .add("i18n/app_fr.properties", new FileInputStream("src/test/resources/integration/app_fr.properties"))
                        .build());
        bundles.add(bundle1);

        Bundle bundle2 = osgi.installAndStart("local:/i18n_de", bundle()
                .add("i18n/app_de.properties", new FileInputStream("src/test/resources/integration/app_de.properties"))
                .build());
        bundles.add(bundle2);

        Map<String, String> messages = service.getAllMessages(Locale.FRENCH);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "mon application")
        );

        messages = service.getAllMessages(Locale.CHINA);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "my application")
        );

        messages = service.getAllMessages(Locale.ENGLISH);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "my application")
        );

        messages = service.getAllMessages(Locale.GERMAN);
        assertThat(messages).contains(
                MapEntry.entry("app.name", "Meine Software")
        );

        bundle1.stop();

        messages = service.getAllMessages(Locale.FRENCH);
        assertThat(messages).doesNotContainKey("app.name");
        messages = service.getAllMessages(Locale.GERMAN);
        assertThat(messages).containsEntry("app.name", "Meine Software");

        bundle2.stop();
        messages = service.getAllMessages(Locale.GERMAN);
        assertThat(messages).doesNotContainEntry("app.name", "Meine Software");
    }
}
