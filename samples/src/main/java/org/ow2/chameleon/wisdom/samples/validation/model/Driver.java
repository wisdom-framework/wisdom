package org.ow2.chameleon.wisdom.samples.validation.model;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;

public class Driver extends Person {
    @Min(value = 18, message = "You have to be 18 to drive a car")
    public int age;
    @AssertTrue(message = "You first have to pass the driving test")
    public boolean hasDrivingLicense;
}
