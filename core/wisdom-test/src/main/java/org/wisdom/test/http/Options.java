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

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.ow2.chameleon.testing.helpers.TimeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores HTTP Client options.
 */
public class Options {

    /**
     * The set of options.
     */
    public enum Option {
        HTTPCLIENT, ASYNCHTTPCLIENT, CONNECTION_TIMEOUT, SOCKET_TIMEOUT, DEFAULT_HEADERS, COOKIES
    }

    /**
     * Default connection timeout.
     */
    public static final long CONNECTION_TIMEOUT = 10000;

    /**
     * Default socket timeout.
     */
    private static final long SOCKET_TIMEOUT = 60000;

    /**
     * The stored options.
     */
    private static final Map<Option, Object> options = new HashMap<>();  //NOSONAR

    /**
     * Sets an option.
     *
     * @param option the option, must not be {@literal null}
     * @param value  the value
     */
    public static void setOption(Option option, Object value) {
        options.put(option, value);
    }

    /**
     * Gets the value of an option.
     *
     * @param option the option, must not be {@literal null}
     * @return the value, {@literal null} if not set
     */
    public static Object getOption(Option option) {
        return options.get(option);
    }

    static {
        // Initialize the options.
        refresh();
    }

    /**
     * Refreshes the options, and restores defaults.
     */
    public static void refresh() {
        // Load timeouts
        Object connectionTimeout = Options.getOption(Option.CONNECTION_TIMEOUT);
        if (connectionTimeout == null) {
            connectionTimeout = CONNECTION_TIMEOUT;
        }

        Object socketTimeout = Options.getOption(Option.SOCKET_TIMEOUT);
        if (socketTimeout == null) {
            socketTimeout = SOCKET_TIMEOUT;
        }

        // Create common default configuration
        final BasicCookieStore store = new BasicCookieStore();
        RequestConfig clientConfig = RequestConfig.custom()
                .setConnectTimeout(((Long) connectionTimeout).intValue() * TimeUtils.TIME_FACTOR)
                .setSocketTimeout(((Long) socketTimeout).intValue()  * TimeUtils.TIME_FACTOR)
                .setConnectionRequestTimeout(((Long) socketTimeout).intValue() * TimeUtils.TIME_FACTOR)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        // Create clients
        setOption(Option.HTTPCLIENT,
                HttpClientBuilder.create()
                        .setDefaultRequestConfig(clientConfig)
                        .setDefaultCookieStore(store)
                        .build()
        );

        setOption(Option.COOKIES, store);

        CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create()
                .setDefaultRequestConfig(clientConfig)
                .setDefaultCookieStore(store)
                .build();
        setOption(Option.ASYNCHTTPCLIENT, asyncClient);
    }

}
