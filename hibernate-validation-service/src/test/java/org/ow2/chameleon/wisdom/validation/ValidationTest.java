package org.ow2.chameleon.wisdom.validation;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 06/11/2013
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */
public class ValidationTest {

    private HibernateValidatorService validation;

    @Before
    public void setUp() {
        validation = new HibernateValidatorService();
        validation.initialize();
    }

    @Test
    public void testPerson() throws Exception {
        Person good = new Person("flore");
        assertThat(validation.validate(good)).isEmpty();

        Person bad = new Person(null);
        assertThat(validation.validate(bad)).hasSize(1);
    }

    @Test
    public void testCar() throws Exception {
        //Case 1 - everything is fine
        Car good = new Car(Lists.newArrayList(new Person("Flore")));
        assertThat(validation.validate(good)).isEmpty();

        // Case 2 - everything is fine, empty list
        good = new Car(Collections.<Person>emptyList());
        assertThat(validation.validate(good)).isEmpty();

        // Case 3 - list null
        Car bad = new Car(null);
        assertThat(validation.validate(bad)).hasSize(1);

        // Case 4 - list not null, but invalid person
        bad = new Car(Lists.newArrayList(new Person(null)));
        assertThat(validation.validate(bad)).hasSize(1);
    }

    @Test
    public void driveAway() {
        // create a car and check that everything is ok with it.
        DrivenCar car = new DrivenCar( "Morris", "DD-AB-123", 2 );
        Set<ConstraintViolation<DrivenCar>> constraintViolations = validation.validate( car );
        assertEquals( 0, constraintViolations.size() );

        // but has it passed the vehicle inspection?
        constraintViolations = validation.validate( car, CarChecks.class );
        assertEquals( 1, constraintViolations.size() );
        assertEquals("The car has to pass the vehicle inspection first", constraintViolations.iterator().next().getMessage());

        // let's go to the vehicle inspection
        car.passedVehicleInspection = true;
        assertEquals( 0, validation.validate( car ).size() );

        // now let's add a driver. He is 18, but has not passed the driving test yet
        Driver john = new Driver( "John Doe" );
        john.setAge( 18 );
        car.driver = john;
        constraintViolations = validation.validate( car, DriverChecks.class );
        assertEquals( 1, constraintViolations.size() );
        assertEquals( "You first have to pass the driving test", constraintViolations.iterator().next().getMessage() );

        // ok, John passes the test
        john.passedDrivingTest( true );
        assertEquals( 0, validation.validate( car, DriverChecks.class ).size() );

        // just checking that everything is in order now
        assertEquals( 0, validation.validate( car, Default.class, CarChecks.class, DriverChecks.class ).size() );
    }
}
