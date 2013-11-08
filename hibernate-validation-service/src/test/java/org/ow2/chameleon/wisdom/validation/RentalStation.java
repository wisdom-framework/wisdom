package org.ow2.chameleon.wisdom.validation;


import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class RentalStation {


        public RentalStation(@NotNull String name, @NotNull String address) {
            //...
        }

        public void rentCar(
                @NotNull String customer,
                @NotNull @Future Date startDate,
                @Min(1) int durationInDays) {
            //...
        }

}
