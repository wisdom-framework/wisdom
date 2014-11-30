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
package org.wisdom.framework.filters.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import com.google.common.net.HttpHeaders;
import org.junit.Test;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.framework.filters.ProxyFilter;
import org.wisdom.test.parents.FakeContext;
import org.wisdom.test.parents.FakeRequest;
import org.wisdom.test.parents.WisdomUnitTest;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyFilterTest extends WisdomUnitTest {

    ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testProxyOnPerdu() throws Exception {
        ProxyFilter filter = new ProxyFilter() {

            @Override
            protected String getProxyTo() {
                return "http://perdu.com";
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) filter.call(route, rc)).callable().call();
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Pas de panique");
        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualTo(MimeTypes.HTML);
    }

    @Test
    public void testQuery() throws Exception {
        ProxyFilter filter = new ProxyFilter() {

            @Override
            protected String getProxyTo() {
                return "http://httpbin.org/get";
            }

            @Override
            protected String getPrefix() {
                return "/proxy";
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/proxy").setHeader(HttpHeaders.CONNECTION, "close");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/proxy?foo=bar&count=1&count=2");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) filter.call(route, rc)).callable().call();
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);

        JsonNode node = mapper.readTree(streamToString(result));
        assertThat(node.get("args").get("count").get(0).asText()).isEqualTo("1");
        assertThat(node.get("args").get("count").get(1).asText()).isEqualTo("2");
        assertThat(node.get("args").get("foo").asText()).isEqualTo("bar");

        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualTo(MimeTypes.JSON);
    }

    @Test
    public void testPostWithData() throws Exception {
        ProxyFilter filter = new ProxyFilter() {

            @Override
            protected String getProxyTo() {
                return "http://httpbin.org/post";
            }

            @Override
            protected String getPrefix() {
                return "/proxy";
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext().setBody("Hello");
        context.setPath("/proxy")
                .setHeader(HttpHeaders.CONNECTION, "close")
                .setHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.TEXT);

        FakeRequest request = new FakeRequest(context).method(HttpMethod.POST).uri("/proxy");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) filter.call(route, rc)).callable().call();
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);

        JsonNode node = mapper.readTree(streamToString(result));
        assertThat(node.get("data").asText()).isEqualTo("Hello");
        assertThat(node.get("headers").get(HeaderNames.CONTENT_LENGTH).asInt()).isEqualTo(5);

        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualTo(MimeTypes.JSON);
    }

    @Test
    public void testFailedRewriting() throws Exception {
        ProxyFilter filter = new ProxyFilter() {

            @Override
            protected String getProxyTo() {
                // Not called, but need to be there.
                return "/";
            }

            @Override
            protected String getPrefix() {
                return "/proxy";
            }

            @Override
            protected Result onRewriteFailed(RequestContext context) {
                return Results.badRequest();
            }

            @Override
            public URI rewriteURI(RequestContext request) throws URISyntaxException {
                // return null on purpose to simulate an error while rewriting the url.
                return null;
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext().setBody("Hello");
        context.setPath("/proxy")
                .setHeader(HttpHeaders.CONNECTION, "close")
                .setHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.TEXT);

        FakeRequest request = new FakeRequest(context).method(HttpMethod.POST).uri("/proxy");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) filter.call(route, rc)).callable().call();
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);
    }

    @Test
    public void testHeaderModification() throws Exception {
        ProxyFilter filter = new ProxyFilter() {

            @Override
            protected String getProxyTo() {
                return "http://httpbin.org/get";
            }

            @Override
            protected void updateHeaders(RequestContext context, Multimap<String, String> headers) {
                headers.put("X-Test", "Test");
            }

            @Override
            protected String getVia() {
                return "wisdom";
            }

            @Override
            protected String getPrefix() {
                return "/proxy";
            }

        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/proxy").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/proxy");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) filter.call(route, rc)).callable().call();
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        JsonNode node = mapper.readTree(streamToString(result));
        assertThat(node.get("headers").get("X-Test").asText()).isEqualTo("Test");
        assertThat(node.get("headers").get("Via").asText()).contains("wisdom");
    }

    @Test
    public void testConfiguration() throws Exception {
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("prefix")).thenReturn("/proxy");
        when(configuration.get("via")).thenReturn("wisdom");
        when(configuration.get("proxyTo")).thenReturn("http://httpbin.org/get");

        ProxyFilter filter = new ProxyFilter(configuration);

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/proxy").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/proxy");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) filter.call(route, rc)).callable().call();
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        JsonNode node = mapper.readTree(streamToString(result));
        assertThat(node.get("headers").get("Via").asText()).contains("wisdom");
    }

    @Test
    public void testPathComputation() throws Exception {
        ProxyFilter filter = new ProxyFilter() {
            @Override
            protected String getPrefix() {
                return "/proxy";
            }

            @Override
            protected String getProxyTo() {
                return "http://example.com";
            }
        };

        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/proxy/foo/bar").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/proxy/foo/bar");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        URI uri = filter.rewriteURI(rc);
        assertThat(uri.toString()).endsWith("/foo/bar");

    }

}