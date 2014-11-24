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
package org.wisdom.test.assertions;

import org.assertj.core.api.AbstractAssert;
import org.wisdom.api.cookies.SessionCookie;

/**
 * Specific AssertJ assertion for {@link org.wisdom.api.cookies.SessionCookie}.
 */
public class SessionCookieAssert extends AbstractAssert<SessionCookieAssert,SessionCookie> {

    protected SessionCookieAssert(SessionCookie actual) {
        super(actual, SessionCookieAssert.class);
    }

    public static SessionCookieAssert assertThat(SessionCookie actual){
        return new SessionCookieAssert(actual);
    }

    //
    // Specific assertions!
    //

    public SessionCookieAssert isEmpty(){
        isNotNull();

        if(!actual.isEmpty()){
            failWithMessage("Expected session to be empty");
        }

        return this;
    }

    public SessionCookieAssert isNotEmpty(){
        isNotNull();

        if(actual.isEmpty()){
            failWithMessage("Expected session not to be empty");
        }

        return this;
    }

    public SessionCookieAssert hasId(String id){
        isNotNull();

        if(!actual.getId().equals(id)){
            failWithMessage("Expected id to be <%s> but was <%s>", id, actual.getId());
        }

        return this;
    }

    public SessionCookieAssert containsEntry(String key, String value){
        isNotNull();
        isNotEmpty();

        if(!value.equals(actual.get(key))){
            failWithMessage("Expected session to contain entry <%s, %s> but value was <%s>",key,value,
                    String.valueOf(actual.get(key)));
        }

        return this;
    }
}
