package org.wisdom.samples.json;

/**
 * Contributor structure
 */
public class Contributor {

    private final String firstName;

    private final String lastName;

    public Contributor(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
