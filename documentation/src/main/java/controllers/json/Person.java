package controllers.json;

import javax.validation.constraints.NotNull;

/**
 * A Person.
 */
public class Person {

    @NotNull
    public String name;
    public int age;
}
