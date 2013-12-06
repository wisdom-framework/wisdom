package org.wisdom.wisit.auth;

/**
 *
 * @author Jonathan M. Bardin
 */
public class Credential {
    public String user;
    public String pass;

    public Credential() {
    }

    public Credential(String user, String pass) {
        this.user=user;
        this.pass=pass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credential credential = (Credential) o;

        if (pass != null ? !pass.equals(credential.pass) : credential.pass != null) return false;
        if (user != null ? !user.equals(credential.user) : credential.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (pass != null ? pass.hashCode() : 0);
        return result;
    }
}
