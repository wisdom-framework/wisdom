package org.ow2.chameleon.wisdom.validation;

import javax.validation.constraints.NotNull;

public class Person {
    @NotNull
    private String name;

    public Person(String name) {
        this.name = name;
    }
    // getters and setters ...
}
