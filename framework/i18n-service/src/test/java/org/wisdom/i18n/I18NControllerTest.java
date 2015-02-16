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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;
import org.wisdom.api.i18n.InternationalizationService;
import org.wisdom.content.jackson.JacksonSingleton;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.*;
import sun.util.resources.cldr.om.LocaleNames_om;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the internationalization controller.
 */
public class I18NControllerTest extends WisdomUnitTest {

    private I18nController controller;

    @Before
    public void setUp() {
        InternationalizationServiceSingleton service = new InternationalizationServiceSingleton(null);
        final Bundle bundle = InternationalizationServiceSingletonTest.getMockBundle();
        service.addingBundle(bundle,
                new BundleEvent(BundleEvent.STARTED, bundle));
        controller = new I18nController();
        controller.service = service;
        final JacksonSingleton jacksonSingleton = new JacksonSingleton();
        jacksonSingleton.validate();
        controller.json = jacksonSingleton;
    }

    @Test
    public void testThatWeCanRetrieveBundles() throws Exception {

        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getBundleResource("Messages_fr");
            }
        }).invoke();

        assertThat(toString(result))
                .contains("welcome=bonjour")
                .contains("lang=français");

        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getBundleResource("Messages.properties");
            }
        }).invoke();

        // No Locale
        assertThat(toString(result))
                .contains("welcome=hello")
                .contains("lang=english");

        // Not provided locale
        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getBundleResource("");
            }
        }).invoke();
        assertThat(status(result)).isEqualTo(Status.NOT_FOUND);
    }

    @Test
    public void testThatWeCanRetrieveJsonBundlesUsedByI18Next() throws Exception {

        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getBundleResourceForI18Next("fr en dev");
            }
        }).invoke();

        final ObjectNode json = json(result);
        System.out.println(json);

        assertThat(json.get("fr")).isNotNull();
        assertThat(json.get("fr").get("translation")).isNotNull();
        assertThat(json.get("fr").get("translation").get("welcome").asText()).isEqualToIgnoringCase("bonjour");
        assertThat(json.get("fr").get("translation").get("app").get("title").asText()).contains("Mon");

        assertThat(json.get("dev")).isNotNull();
        assertThat(json.get("dev").get("translation")).isNotNull();
        assertThat(json.get("dev").get("translation").get("welcome").asText()).isEqualToIgnoringCase("hello");
        assertThat(json.get("dev").get("translation").get("app").get("title").asText()).contains("My");
    }

    @Test
    public void testThatWeCanRetrieveMessagesIndividuallyUsingQueryParameter() throws Exception {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessage("welcome", Locale.ENGLISH);
            }
        }).invoke();
        assertThat(toString(result)).isEqualTo("hello");

        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessage("welcome", Locale.FRENCH);
            }
        }).invoke();
        assertThat(toString(result)).isEqualTo("bonjour");

        // Not supported locale, delegate to default
        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessage("welcome", Locale.FRANCE);
            }
        }).invoke();
        assertThat(toString(result)).isEqualTo("hello");
    }

    @Test
    public void testThatWeCanRetrieveMessagesIndividuallyUsingHttpHeader() throws Exception {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessage("welcome", null);
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "en")
                .invoke();
        assertThat(toString(result)).isEqualTo("hello");

        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessage("welcome", null);
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "fr")
                .invoke();
        assertThat(toString(result)).isEqualTo("bonjour");
    }

    @Test
    public void testThatWeCanRetrieveAllMessagesUsingHeader() throws Exception {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessages(null);
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "en")
                .invoke();
        assertThat(toString(result)).contains("hello");


        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessages(null);
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "fr")
                .invoke();
        assertThat(toString(result)).contains("bonjour");


        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessages(null);
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "fr-FR")
                .invoke();
        assertThat(toString(result)).contains("hello");

        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessages(null);
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "fr_FR")
                .invoke();
        assertThat(toString(result)).contains("hello");
    }

    @Test
    public void testThatWeCanRetrieveAllMessagesUsingQueryParameter() throws Exception {
        Action.ActionResult result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessages(ImmutableList.of(Locale.ENGLISH));
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "fr")
                .invoke();
        assertThat(toString(result)).contains("hello");

        result = Action.action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.getMessages(ImmutableList.of(Locale.FRENCH));
            }
        })
                .header(HeaderNames.ACCEPT_LANGUAGE, "en")
                .invoke();
        assertThat(toString(result)).contains("bonjour");
    }


}
