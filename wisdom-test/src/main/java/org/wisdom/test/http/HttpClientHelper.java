package org.wisdom.test.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.wisdom.api.http.HttpMethod;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClientHelper {

    private static final String USER_AGENT = "wisdom-test/1.1";

    private HttpClientHelper(){
        //Unused
    }

    private static <T> FutureCallback<org.apache.http.HttpResponse> prepareCallback(final Class<T> responseClass,
            final Callback<T> callback) {
        if (callback == null){
            return null;
        }

        return new FutureCallback<org.apache.http.HttpResponse>() {

            public void cancelled() {
                callback.cancelled();
            }

            public void completed(org.apache.http.HttpResponse arg0) {
                callback.completed(new HttpResponse<T>(arg0, responseClass));
            }

            public void failed(Exception arg0) {
                callback.failed(arg0);
            }

        };
    }

    public static <T> Future<HttpResponse<T>> requestAsync(HttpRequest request, final Class<T> responseClass, Callback<T> callback) {
        HttpUriRequest requestObj = prepareRequest(request);

        CloseableHttpAsyncClient asyncHttpClient = ClientFactory.getAsyncHttpClient();
        if (!asyncHttpClient.isRunning()) {
            asyncHttpClient.start();
        }

        final Future<org.apache.http.HttpResponse> future = asyncHttpClient.execute(requestObj,
                prepareCallback(responseClass, callback));

        return new Future<HttpResponse<T>>() {

            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            public boolean isCancelled() {
                return future.isCancelled();
            }

            public boolean isDone() {
                return future.isDone();
            }

            public HttpResponse<T> get() throws InterruptedException, ExecutionException {
                org.apache.http.HttpResponse httpResponse = future.get();
                return new HttpResponse<T>(httpResponse, responseClass);
            }

            public HttpResponse<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
                org.apache.http.HttpResponse httpResponse = future.get(timeout, unit);
                return new HttpResponse<T>(httpResponse, responseClass);
            }
        };
    }

    public static <T> HttpResponse<T> request(HttpRequest request, Class<T> responseClass) throws Exception {
        HttpRequestBase requestObj = prepareRequest(request);
        // The DefaultHttpClient is thread-safe
        HttpClient client = ClientFactory.getHttpClient(); 

        org.apache.http.HttpResponse response;
        try {
            response = client.execute(requestObj);
            HttpResponse<T> httpResponse = new HttpResponse<T>(response, responseClass);
            requestObj.releaseConnection();
            return httpResponse;
        } finally {
            requestObj.releaseConnection();
        }
    }

    private static HttpRequestBase prepareRequest(HttpRequest request) {

        request.header("user-agent", USER_AGENT);
        request.header("accept-encoding", "gzip");

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

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            reqObj.addHeader(entry.getKey(), entry.getValue());
        }

        // Set body
        if (request.getHttpMethod() != HttpMethod.GET && request.getBody() != null) {
            ((HttpEntityEnclosingRequestBase) reqObj).setEntity(request.getBody().getEntity());
        }

        return reqObj;
    }
}
