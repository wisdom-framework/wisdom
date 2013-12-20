package org.wisdom.test.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;


public class ClientFactory {

    public static HttpClient getHttpClient() {
        return (HttpClient) Options.getOption(Options.Option.HTTPCLIENT);
    }

    public static CloseableHttpAsyncClient getAsyncHttpClient() {
        return (CloseableHttpAsyncClient) Options.getOption(Options.Option.ASYNCHTTPCLIENT);
    }

}
