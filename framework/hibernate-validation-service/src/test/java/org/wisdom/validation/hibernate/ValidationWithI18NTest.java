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
package org.wisdom.validation.hibernate;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.i18n.InternationalizationService;
import org.wisdom.test.parents.FakeContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the validator service when using the internationalization.
 */
public class ValidationWithI18NTest {

    private Validator validator;
    private Locale defaultLocale;

    @Before
    public void setUp() {
        final HibernateValidatorService service = new HibernateValidatorService();
        service.interpolator = new ConstraintMessageInterpolator();
        service.interpolator.i18n = mock(InternationalizationService.class);
        when(service.interpolator.i18n.get(Locale.US, "name_must_be_set")).thenReturn("The name must be set");
        when(service.interpolator.i18n.get(Locale.FRENCH, "name_must_be_set")).thenReturn("Le nom doit etre" +
                " spécifié");
        when(service.interpolator.i18n.get(Locale.US, "name_too_short")).thenReturn("The name '${validatedValue}' " +
                "must contain at least {min} characters long");
        when(service.interpolator.i18n.get(Locale.FRENCH, "name_too_short")).thenReturn("Le nom '${validatedValue}' " +
                "est trop court");
        Validator delegate = service.initialize();
        validator = new WrappedValidator(delegate);
    }

    @Test
    public void testInternationalization() throws Exception {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        Person good = new Person("flore");
        assertThat(validator.validate(good)).isEmpty();

        // First test with default locale
        Person bad = new Person(null);
        assertThat(validator.validate(bad)).hasSize(1);
        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("The name must be set");

        FakeContext ctxt = new FakeContext().setHeader(HeaderNames.ACCEPT_LANGUAGE, "fr");
        Context.CONTEXT.set(ctxt);

        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("Le nom doit etre spécifié");

        ctxt = new FakeContext().setHeader(HeaderNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8,de;q=0.6,fr;q=0.4");
        Context.CONTEXT.set(ctxt);

        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("The name must be set");

    }

    @Test
    public void testInternationalizationWithInterpolation() throws Exception {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        InternationalizedPerson good = new InternationalizedPerson("flore");
        assertThat(validator.validate(good)).isEmpty();

        // First test with default locale
        InternationalizedPerson bad = new InternationalizedPerson("ts");
        assertThat(validator.validate(bad)).hasSize(1);
        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("'ts'")
                .contains("must contain at least 4 characters long");

        FakeContext ctxt = new FakeContext().setHeader(HeaderNames.ACCEPT_LANGUAGE, "fr");
        Context.CONTEXT.set(ctxt);

        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("'ts'")
                .contains("est trop court");

        ctxt = new FakeContext().setHeader(HeaderNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8,de;q=0.6,fr;q=0.4");
        Context.CONTEXT.set(ctxt);

        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("'ts'")
                .contains("must contain at least 4 characters long");

    }

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
        if (defaultLocale != null) {
            Locale.setDefault(defaultLocale);
        }
    }
}
