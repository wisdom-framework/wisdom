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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.test.WisdomRunner;
import org.wisdom.test.parents.FakeContext;

import javax.inject.Inject;
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

/**
 * Test the validator service.
 */
@RunWith(WisdomRunner.class)
public class ValidationIT {

    @Inject
    private Validator validator;

    @Test
    public void testPerson() throws Exception {
        Person good = new Person("flore");
        assertThat(validator.validate(good)).isEmpty();

        Person bad = new Person(null);
        assertThat(validator.validate(bad)).hasSize(1);
    }

    @Test
    public void testCar() throws Exception {
        //Case 1 - everything is fine
        Car good = new Car(Lists.newArrayList(new Person("Flore")));
        assertThat(validator.validate(good)).isEmpty();

        // Case 2 - everything is fine, empty list
        good = new Car(Collections.<Person>emptyList());
        assertThat(validator.validate(good)).isEmpty();

        // Case 3 - list null
        Car bad = new Car(null);
        assertThat(validator.validate(bad)).hasSize(1);

        // Case 4 - list not null, but invalid person
        bad = new Car(Lists.newArrayList(new Person(null)));
        assertThat(validator.validate(bad)).hasSize(1);
    }

    @Test
    public void driveAway() {
        // create a car and check that everything is ok with it.
        DrivenCar car = new DrivenCar( "Morris", "DD-AB-123", 2 );
        Set<ConstraintViolation<DrivenCar>> constraintViolations = validator.validate( car );
        assertEquals( 0, constraintViolations.size() );

        // but has it passed the vehicle inspection?
        constraintViolations = validator.validate( car, CarChecks.class );
        assertEquals( 1, constraintViolations.size() );
        assertEquals("The car has to pass the vehicle inspection first", constraintViolations.iterator().next().getMessage());

        // let's go to the vehicle inspection
        car.passedVehicleInspection = true;
        assertEquals( 0, validator.validate( car ).size() );

        // now let's add a driver. He is 18, but has not passed the driving test yet
        Driver john = new Driver( "John Doe" );
        john.setAge( 18 );
        car.driver = john;
        constraintViolations = validator.validate( car, DriverChecks.class );
        assertEquals( 1, constraintViolations.size() );
        assertEquals( "You first have to pass the driving test", constraintViolations.iterator().next().getMessage() );

        // ok, John passes the test
        john.passedDrivingTest( true );
        assertEquals( 0, validator.validate( car, DriverChecks.class ).size() );

        // just checking that everything is in order now
        assertEquals( 0, validator.validate( car, Default.class, CarChecks.class, DriverChecks.class ).size() );
    }

    @Test
    public void testConstructorParameters() throws NoSuchMethodException {
        ExecutableValidator executableValidator = validator.forExecutables();
        Constructor<RentalStation> constructor = RentalStation.class.getConstructor(String.class, String.class);
        Set<ConstraintViolation<RentalStation>> violations = executableValidator.validateConstructorParameters
                (constructor, new Object[] {"Hertz", ""});
        assertThat(violations.size()).isEqualTo(0);
        violations = executableValidator.validateConstructorParameters
                (constructor, new Object[] {null, ""});
        assertThat(violations.size()).isEqualTo(1);
        violations = executableValidator.validateConstructorParameters
                (constructor, new Object[] {null, null});
        assertThat(violations.size()).isEqualTo(2);
    }

    @Test
    public void testMethodParameters() throws NoSuchMethodException {
        ExecutableValidator executableValidator = validator.forExecutables();
        Method method = RentalStation.class.getMethod("rentCar", String.class, Date.class,
                Integer.TYPE);
        RentalStation station = new RentalStation("hertz", "");
        Set<ConstraintViolation<RentalStation>> violations = executableValidator.validateParameters(
                station, method, new Object[]{"Clement", new Date(System.currentTimeMillis() + 10000), 1});
        assertThat(violations.size()).isEqualTo(0);
        violations = executableValidator.validateParameters(
                station, method, new Object[]{"Clement", new Date(System.currentTimeMillis() - 10000), 1});
        assertThat(violations.size()).isEqualTo(1);
        violations = executableValidator.validateParameters(
                station, method, new Object[]{"Clement", new Date(System.currentTimeMillis() - 10000), 0});
        assertThat(violations.size()).isEqualTo(2);
        violations = executableValidator.validateParameters(
                station, method, new Object[]{null, new Date(System.currentTimeMillis() - 10000), 0});
        assertThat(violations.size()).isEqualTo(3);
    }

    @Test
    public void testInternationalizationWithInterpolation() throws Exception {
        InternationalizedPerson good = new InternationalizedPerson("flore");
        assertThat(validator.validate(good)).isEmpty();

        // With null, default locale
        InternationalizedPerson bad = new InternationalizedPerson(null);
        assertThat(validator.validate(bad)).hasSize(1);
        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("The name must be set");

        // With null, French locale
        FakeContext ctxt = new FakeContext().setHeader(HeaderNames.ACCEPT_LANGUAGE, "fr");
        Context.CONTEXT.set(ctxt);
        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("Le nom doit être spécifié");

        // With name too short, english locale
        ctxt = new FakeContext().setHeader(HeaderNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8,de;q=0.6,fr;q=0.4");
        Context.CONTEXT.set(ctxt);
        bad = new InternationalizedPerson("ts");
        assertThat(validator.validate(bad)).hasSize(1);
        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("'ts'")
                .contains("must contain at least 4 characters long");


        // With name too short, French locale
        ctxt = new FakeContext().setHeader(HeaderNames.ACCEPT_LANGUAGE, "fr");
        Context.CONTEXT.set(ctxt);
        assertThat(validator.validate(bad).iterator().next().getMessage()).contains("'ts'")
                .contains("est trop court");
    }

    @Test
    public void testSizeAndLengthConstraints() throws Exception {
        Stuff good = new Stuff("abcd", "abcd");
        assertThat(validator.validate(good)).isEmpty();

        Stuff bad = new Stuff("a", "a");
        assertThat(validator.validate(bad)).hasSize(2);
    }
}
