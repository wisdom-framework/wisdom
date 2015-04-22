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
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.ow2.chameleon.testing.helpers.TimeUtils;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A couple of method easing emitting HTTP requests.
 */
public final class HttpClientHelper {

    /**
     * A fake user agent.
     */
    private static final String USER_AGENT = "wisdom-test/1.1";

    private HttpClientHelper() {
        //Unused
    }

    private static <T> FutureCallback<org.apache.http.HttpResponse> prepareCallback(final Class<T> responseClass,
                                                                                    final Callback<T> callback) {
        if (callback == null) {
            return null;
        }

        return new FutureCallback<org.apache.http.HttpResponse>() {

            /**
             * Cancels the request.
             */
            public void cancelled() {
                callback.cancelled();
            }

            /**
             * Completes the request.
             * @param arg0 the HTTP Response
             */
            public void completed(org.apache.http.HttpResponse arg0) {
                callback.completed(new HttpResponse<>(arg0, responseClass));
            }

            /**
             * Marks the request as failed.
             * @param arg0 the error
             */
            public void failed(Exception arg0) {
                callback.failed(arg0);
            }

        };
    }

    /**
     * Emits an asynchronous request.
     *
     * @param request       the request
     * @param responseClass the response class
     * @param callback      the completion callback
     * @param <T>           the type of the expected result
     * @return the future to retrieve the result
     */
    public static <T> Future<HttpResponse<T>> requestAsync(HttpRequest request, final Class<T> responseClass, Callback<T> callback) {
        HttpUriRequest requestObj = prepareRequest(request);

        CloseableHttpAsyncClient asyncHttpClient = ClientFactory.getAsyncHttpClient();
        if (!asyncHttpClient.isRunning()) {
            asyncHttpClient.start();
        }

        final Future<org.apache.http.HttpResponse> future = asyncHttpClient.execute(requestObj,
                prepareCallback(responseClass, callback));

        return new Future<HttpResponse<T>>() {

            /**
             * Cancels the request.
             *
             * @param mayInterruptIfRunning whether or not we need to interrupt the request.
             * @return {@literal true} if the task is successfully canceled.
             */
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            /**
             * @return whether the future is cancelled.
             */
            public boolean isCancelled() {
                return future.isCancelled();
            }

            /**
             * @return whether the result is available.
             */
            public boolean isDone() {
                return future.isDone();
            }

            /**
             * Gets the result.
             * @return the response.
             * @throws InterruptedException if the request is interrupted.
             * @throws ExecutionException if the request fails.
             */
            public HttpResponse<T> get() throws InterruptedException, ExecutionException {
                org.apache.http.HttpResponse httpResponse = future.get();
                return new HttpResponse<>(httpResponse, responseClass);
            }

            /**
             * Gets the result.
             * @param timeout timeout configuration
             * @param unit unit timeout
             * @return the response.
             * @throws InterruptedException if the request is interrupted.
             * @throws ExecutionException if the request fails.
             * @throws TimeoutException if the set time out is reached before the completion of the request.
             */
            public HttpResponse<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                    TimeoutException {
                org.apache.http.HttpResponse httpResponse = future.get(timeout * TimeUtils.TIME_FACTOR, unit);
                return new HttpResponse<>(httpResponse, responseClass);
            }
        };
    }

    /**
     * Executes the request.
     *
     * @param request       the request
     * @param responseClass the response class
     * @param <T>           the type of content expected in the response
     * @return the response
     * @throws Exception if something bad happens.
     */
    public static <T> HttpResponse<T> request(HttpRequest request, Class<T> responseClass) throws Exception {
        HttpRequestBase requestObj = prepareRequest(request);
        // The DefaultHttpClient is thread-safe
        HttpClient client = ClientFactory.getHttpClient();

        org.apache.http.HttpResponse response;
        try {
            response = client.execute(requestObj);
            HttpResponse<T> httpResponse = new HttpResponse<>(response, responseClass);
            requestObj.releaseConnection();
            return httpResponse;
        } finally {
            requestObj.releaseConnection();
        }
    }

    private static HttpRequestBase prepareRequest(HttpRequest request) {

        if(!request.getHeaders().containsKey(HeaderNames.USER_AGENT)){
            request.header(HeaderNames.USER_AGENT, USER_AGENT);
        }

        if(!request.getHeaders().containsKey(HeaderNames.ACCEPT_ENCODING)) {
            request.header(HeaderNames.ACCEPT_ENCODING, "gzip");
        }

        Object defaultHeaders = Options.getOption(Options.Option.DEFAULT_HEADERS);
        if (defaultHeaders != null) {
            @SuppressWarnings("unchecked")
            Set<Map.Entry<String, String>> entrySet = ((Map<String, String>) defaultHeaders).entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                request.header(entry.getKey(), entry.getValue());
            }
        }

        HttpRequestBase reqObj = null;

        switch (request.getHttpMethod()) {
            case GET:
                reqObj = new HttpGet(request.getUrl());
                break;
            case POST:
                reqObj = new HttpPost(request.getUrl());
                break;
            case PUT:
                reqObj = new HttpPut(request.getUrl());
                break;
            case DELETE:
                reqObj = new HttpDeleteWithBody(request.getUrl());
                break;
            case OPTIONS:
                reqObj = new HttpOptions(request.getUrl());
                break;
            default:
                break;
        }

        if(reqObj == null) {
            throw new IllegalStateException("Cannot build the request - unsupported HTTP verb : " + request
                    .getHttpMethod());
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            reqObj.addHeader(entry.getKey(), entry.getValue());
        }

        // Set body
        if (request.getHttpMethod() != HttpMethod.GET && request.getBody() != null
                && reqObj instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) reqObj).setEntity(request.getBody().getEntity());
        }

        return reqObj;
    }
}
