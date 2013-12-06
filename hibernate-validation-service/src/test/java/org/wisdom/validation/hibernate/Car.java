package org.wisdom.validation.hibernate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class Car {

    @NotNull
    @Valid
    private List<Person> passengers = new ArrayList<Person>();

    public Car(List<Person> passengers) {
        this.passengers = passengers;
    }

}
