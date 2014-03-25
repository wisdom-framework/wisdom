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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

import java.util.HashMap;
import java.util.Map;

public class Options {

    public enum Option {
        HTTPCLIENT, ASYNCHTTPCLIENT, CONNECTION_TIMEOUT, SOCKET_TIMEOUT, DEFAULT_HEADERS
    }

    public static final long CONNECTION_TIMEOUT = 10000;
    private static final long SOCKET_TIMEOUT = 60000;

    private static Map<Option, Object> options = new HashMap<>();

    public static void setOption(Option option, Object value) {
        options.put(option, value);
    }

    public static Object getOption(Option option) {
        return options.get(option);
    }

    static {
        refresh();
    }

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
        RequestConfig clientConfig = RequestConfig.custom().setConnectTimeout(((Long) connectionTimeout).intValue()).setSocketTimeout(((Long) socketTimeout).intValue()).setConnectionRequestTimeout(((Long) socketTimeout).intValue()).build();

        // Create clients
        setOption(Option.HTTPCLIENT, HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).build());

        CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create().setDefaultRequestConfig(clientConfig).build();
        setOption(Option.ASYNCHTTPCLIENT, asyncClient);
    }

}
