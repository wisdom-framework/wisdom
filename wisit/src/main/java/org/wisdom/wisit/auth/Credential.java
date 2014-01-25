package org.wisdom.wisit.auth;

/**
 *
 * @author Jonathan M. Bardin
 */
public class Credential {
    private String user;
    private String pass;

    public Credential() {
        //Default
    }
    
    public Credential(String user, String pass) {
        this.user=user;
        this.pass=pass;
    }

    public String getUser(){
        return this.user;
    }
    public void setUser(String user){
        this.user = user;
    }

    public String getPass(){
        return this.pass;
    }

    public void setPass(String pass){
        this.pass = pass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (o == null || getClass() != o.getClass()){
            return false;
        }

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
