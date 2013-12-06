package org.wisdom.router;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Fake Form
 */
public class Form {

    @NotNull
    public String name;

    @Email @NotNull
    public String email;

    @Min(18)
    public int age;
}
