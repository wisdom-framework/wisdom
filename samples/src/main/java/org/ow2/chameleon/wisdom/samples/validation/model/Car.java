package org.ow2.chameleon.wisdom.samples.validation.model;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Car {
    @NotNull
    public String manufacturer;

    @NotNull
    @Size(min = 2, max = 14)
    public String licensePlate;

    @Min(2)
    public int seatCount;

    @Valid
    public Driver driver;

}
