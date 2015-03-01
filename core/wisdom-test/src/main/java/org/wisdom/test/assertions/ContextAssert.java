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
import org.wisdom.api.http.Context;

/**
 * Specific AssertJ Assertion for {@link org.wisdom.api.http.Context}
 */
public class ContextAssert extends AbstractAssert<ContextAssert, Context> {

    protected ContextAssert(Context actual) {
        super(actual, ContextAssert.class);
    }

    public static ContextAssert assertThat(Context actual){
        return new ContextAssert(actual);
    }

    //
    // Specific assertions!
    //

    public ContextAssert hasInSession(String key, String value){
        isNotNull();
        SessionAssert.assertThat(actual.session()).containsEntry(key,value);

        return this;
    }

    public ContextAssert hasId(Long id){
        isNotNull();

        if(!actual.id().equals(id)){
            failWithMessage("Expected id to be <%s> but was <%s>", id, actual.id());

        }

        return this;
    }

    public ContextAssert isMultipart(){
        isNotNull();

        if(!actual.isMultipart()){
            failWithMessage("Expected to be multi-part");
        }

        return this;
    }

    public ContextAssert isNotMultipart(){
        isNotNull();

        if(actual.isMultipart()){
            failWithMessage("Expected NOT to be multi-part");
        }

        return this;
    }

    public ContextAssert hasContextPath(String path){
        isNotNull();

        if(!actual.contextPath().equals(path)){
            failWithMessage("Expected body to be <%n%s%n> but was <%n%s%n>", path, actual.contextPath());
        }

        return this;
    }

    public ContextAssert hasInBody(String inBody){
        isNotNull();

        if(!actual.body().contains(inBody)){
            failWithMessage("Expected body to contain <%s> but body is <%s>", inBody, actual.body());
        }

        return this;
    }

    public ContextAssert hasBodyMatch(String regex){
        isNotNull();

        if(!actual.body().matches(regex)){
            failWithMessage("Expected body to match <%s> but body is <%s>", regex, actual.body());
        }

        return this;
    }

    public ContextAssert hasBody(String body){
        isNotNull();

        if(!actual.body().equals(body)){
            failWithMessage("Expected body to be <%s> but was <%s>", body, actual.body());
        }

        return this;
    }

    public <T> ContextAssert hasBody(Class<T> klass, T body){
        isNotNull();

        if(!actual.body(klass).equals(body)){
            failWithMessage("Expected body to be <%s> but was <%s>", body.toString(), actual.body(klass).toString());
        }

        return this;
    }
}
