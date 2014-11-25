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
import org.wisdom.test.http.HttpResponse;

/**
 * Specific AssertJ Assertion for {@link org.wisdom.test.http.HttpResponse}
 */
public class HttpResponseAssert<T> extends AbstractAssert<HttpResponseAssert<T>,HttpResponse<T>>{

    protected HttpResponseAssert(HttpResponse<T> actual) {
        super(actual, HttpResponseAssert.class);
    }

    public static <T> HttpResponseAssert<T> assertThat(HttpResponse<T> actual){
        return new HttpResponseAssert<>(actual);
    }

    //
    // Specific Assertions!
    //


    public HttpResponseAssert<T> hasInHeader(String key,String value){
        isNotNull();

        if(!value.equals(actual.headers().get(key))){
            failWithMessage("Expected header to contain entry <%s, %s> but value was <%s>",key,value,
                    String.valueOf(actual.headers().get(key)));
        }

        return this;
    }

    public HttpResponseAssert<T> hasCookie(String cookieName){
        isNotNull();

        if(actual.cookie(cookieName) == null){
            failWithMessage("Expected to contain a cookie with name <%s>",cookieName);
        }

        return this;
    }


    public HttpResponseAssert<T> hasContentType(String contentType){
        isNotNull();

        if(!actual.contentType().equals(contentType)){
            failWithMessage("Expected content type to be <%s> but was <%s>",contentType,actual.contentType());
        }

        return this;
    }

    public HttpResponseAssert<T> hasCode(int code){
        isNotNull();

        if(code != actual.code()){
            failWithMessage("Expected status code to be <%n> but was <%n>",code,actual.code());
        }

        return this;
    }

    public HttpResponseAssert<T> hasInBody(String  inBody){
        isNotNull();

        if(!(actual.body() instanceof String)){
            failWithMessage("Body is not an instance of String, body is <%s>",actual.body().toString());

        }

        if(!((String) actual.body()).contains(inBody)){
            failWithMessage("Expected body to contain <%s>, but body is <%s>",inBody,actual.body().toString());
        }

        return this;
    }

    public HttpResponseAssert<T> bodyMatches(String  regex){
        isNotNull();

        if(!(actual.body() instanceof String)){
            failWithMessage("Body is not an instance of String, body is <%s>",actual.body().toString());

        }

        if(!((String) actual.body()).matches(regex)){
            failWithMessage("Expected body to match <%s>, but body is <%s>",regex,actual.body().toString());
        }

        return this;
    }

    public HttpResponseAssert<T> hasBody(T body){
        isNotNull();

        if(!body.equals(actual.body())){
            failWithMessage("Expected body to be <%s> but was <%s>",body.toString(),actual.body().toString());
        }

        return this;
    }

    public HttpResponseAssert<T> hasLength(Integer length){
        isNotNull();

        if(actual.length() != length){
            failWithMessage("Expected length to be <%s> but was <%s>",length,actual.length());
        }

        return this;
    }

    public HttpResponseAssert<T> hasCharset(String charset){
        isNotNull();

        if(!actual.charset().equals(charset)){
            failWithMessage("Expected charset to be <%s> but was <%s>",charset,actual.charset());
        }

        return this;
    }
}
