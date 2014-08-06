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
package org.wisdom.test.http;

import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.util.List;


/**
 * The HTTP Client factory.
 */
public final class ClientFactory {

    private ClientFactory() {
        //Hide implicit constructor
    }

    /**
     * Retrieves the regular (synchronous) HTTP Client instance.
     *
     * @return the HTTP Client instance
     */
    public static HttpClient getHttpClient() {
        return (HttpClient) Options.getOption(Options.Option.HTTPCLIENT);
    }

    /**
     * Retrieves the asynchronous HTTP Client instance.
     *
     * @return the asynchronous HTTP Client instance
     */
    public static CloseableHttpAsyncClient getAsyncHttpClient() {
        return (CloseableHttpAsyncClient) Options.getOption(Options.Option.ASYNCHTTPCLIENT);
    }

    /**
     * Retrieves the set of cookie currently used by the HTTP Client.
     *
     * @return the list of cookies
     */
    public static List<Cookie> getCookies() {
        return ((BasicCookieStore) Options.getOption(Options.Option.COOKIES)).getCookies();
    }

}
