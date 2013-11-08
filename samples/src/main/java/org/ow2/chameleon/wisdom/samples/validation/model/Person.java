package org.ow2.chameleon.wisdom.samples.validation.model;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;

public class Person {
    @NotNull
    public String name;

    @NotNull
    @Email
    public String email;

}
