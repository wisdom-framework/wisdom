/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.wisit.auth;

/**
 * An over simple Credential class used in order to identify a shell user.
 *
 * @author Jonathan M. Bardin
 */
public class Credential {
    private String user;
    private String pass;

    /**
     * Empty constructor.
     */
    public Credential() {
        //Default
    }

    /**
     * Create a new Credential with the username and password as argument.
     *
     * @param user the username
     * @param pass the user password
     */
    public Credential(String user, String pass) {
        this.user=user;
        this.pass=pass;
    }

    /**
     * @return The username.
     */
    public String getUser(){
        return this.user;
    }

    /**
     * @param user the username.
     */
    public void setUser(String user){
        this.user = user;
    }

    /**
     * @return the user password.
     */
    public String getPass(){
        return this.pass;
    }

    /**
     * @param pass the user password.
     */
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
