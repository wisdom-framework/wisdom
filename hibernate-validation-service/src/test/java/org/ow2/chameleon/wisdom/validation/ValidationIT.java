package org.ow2.chameleon.wisdom.validation;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.chameleon.wisdom.test.WisdomRunner;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Collections;
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
}
