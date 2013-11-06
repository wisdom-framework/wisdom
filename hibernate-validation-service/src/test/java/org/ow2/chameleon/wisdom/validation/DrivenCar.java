package org.ow2.chameleon.wisdom.validation;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 06/11/2013
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */
public class DrivenCar {
    @NotNull
    private String manufacturer;

    @NotNull
    @Size(min = 2, max = 14)
    private String licensePlate;

    @Min(2)
    private int seatCount;

    @AssertTrue(message = "The car has to pass the vehicle inspection first", groups = CarChecks.class)
    public boolean passedVehicleInspection;

    @Valid
    public Driver driver;

    public DrivenCar(String manufacturer, String licencePlate, int seatCount) {
        this.manufacturer = manufacturer;
        this.licensePlate = licencePlate;
        this.seatCount = seatCount;
    }
}
