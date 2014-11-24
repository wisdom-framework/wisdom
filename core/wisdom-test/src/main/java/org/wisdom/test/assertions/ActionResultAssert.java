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
import org.wisdom.test.parents.Action;

import java.nio.charset.Charset;

/**
 * Specific AssertJ Assertions for {@link org.wisdom.test.parents.Action.ActionResult}.
 */
public class ActionResultAssert extends AbstractAssert<ActionResultAssert,Action.ActionResult> {

    protected ActionResultAssert(Action.ActionResult actual) {
        super(actual, ActionResultAssert.class);
    }

    public static ActionResultAssert assertThat(Action.ActionResult actual){
        return new ActionResultAssert(actual);
    }

    //
    // Specific assertions!
    //

    public StatusAssert status(){
        isNotNull();
        return StatusAssert.assertThat(actual.getResult().getStatusCode());
    }

    public ActionResultAssert hasStatus(int statusCode){
        isNotNull();

        if(actual.getResult().getStatusCode() != statusCode){
            failWithMessage("Expected status to be <%n> but was <%n>", statusCode, actual.getResult().getStatusCode());
        }

        return this;
    }

    public ActionResultAssert hasContentType(String contentType){
        isNotNull();

        if(!actual.getResult().getContentType().equals(contentType)){
            failWithMessage("Expected content type to be <%s> but was <%s>", contentType,
                    actual.getResult().getContentType());
        }

        return this;
    }

    public ActionResultAssert hasFullContentType(String fullContentType){
        isNotNull();

        if(!actual.getResult().getFullContentType().equals(fullContentType)){
            failWithMessage("Expected content type to be <%s> but was <%s>", fullContentType,
                    actual.getResult().getFullContentType());
        }

        return this;
    }

    public ActionResultAssert hasCharset(Charset charset){
        isNotNull();

        if(!actual.getResult().getCharset().equals(charset)){
            failWithMessage("Expected charset to be <%s> but was <%s>", charset.displayName(),
                    actual.getResult().getCharset().displayName());
        }

        return this;
    }

    //
    // Delegate to SessionCookieAssert
    //

    public ActionResultAssert hasInSession(String key, String value){
        isNotNull();
        SessionCookieAssert.assertThat(actual.getContext().session()).containsEntry(key,value);

        return this;
    }

    public ActionResultAssert hasSessionId(String id){
        isNotNull();
        SessionCookieAssert.assertThat(actual.getContext().session()).hasId(id);

        return this;
    }

    public ActionResultAssert sessionIsEmpty(){
        isNotNull();
        SessionCookieAssert.assertThat(actual.getContext().session()).isEmpty();

        return this;
    }

    public ActionResultAssert sessionIsNotEmpty(){
        isNotNull();
        SessionCookieAssert.assertThat(actual.getContext().session()).isNotEmpty();

        return this;
    }

    //
    // Delegate to ContextAssert
    //


    @Override
    public ActionResultAssert isNotNull() {
        super.isNotNull();

        if(actual.getResult() == null){
            failWithMessage("Result should not be null");
        }

        return this;
    }
}
