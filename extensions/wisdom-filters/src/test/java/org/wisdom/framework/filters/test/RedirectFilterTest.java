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

import com.google.common.net.HttpHeaders;
import org.junit.Test;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.framework.filters.RedirectFilter;
import org.wisdom.test.parents.FakeContext;
import org.wisdom.test.parents.FakeRequest;
import org.wisdom.test.parents.WisdomUnitTest;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RedirectFilterTest extends WisdomUnitTest {

    @Test
    public void testRedirection() throws Exception {
        RedirectFilter filter = new RedirectFilter() {

            @Override
            protected String getRedirectTo() {
                return "http://perdu.com";
            }

            @Override
            protected String getPrefix() {
                return "/";
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = filter.call(route, rc);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.SEE_OTHER);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("http://perdu.com");
    }

    @Test
    public void testRedirectionWithQuery() throws Exception {
        RedirectFilter filter = new RedirectFilter() {

            @Override
            protected String getRedirectTo() {
                return "http://perdu.com";
            }

            @Override
            protected String getPrefix() {
                return "/";
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/?foo=bar");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = filter.call(route, rc);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.SEE_OTHER);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("http://perdu.com?foo=bar");
    }

    @Test
    public void testRedirectionForPostRequests() throws Exception {
        RedirectFilter filter = new RedirectFilter() {

            @Override
            protected String getRedirectTo() {
                return "http://perdu.com";
            }

            @Override
            protected String getPrefix() {
                return "/";
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.POST).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = filter.call(route, rc);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.SEE_OTHER);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("http://perdu.com");
    }

    @Test
    public void testFailedRewriting() throws Exception {
        RedirectFilter filter = new RedirectFilter() {

            @Override
            protected String getRedirectTo() {
                return "http://perdu.com";
            }

            @Override
            protected String getPrefix() {
                return "/";
            }

            @Override
            protected Result onRewriteFailed(RequestContext context) {
                return Results.badRequest();
            }

            @Override
            public URI rewriteURI(Request request) throws URISyntaxException {
                // return null on purpose to simulate an error while rewriting the url.
                return null;
            }
        };

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = filter.call(route, rc);
        assertThat(result).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);
    }

    @Test
    public void testConfiguration() throws Exception {
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("prefix")).thenReturn("/redirected");
        when(configuration.get("redirectTo")).thenReturn("http://perdu.com");

        RedirectFilter filter = new RedirectFilter(configuration);

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/redirected").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/redirected");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = filter.call(route, rc);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.SEE_OTHER);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("http://perdu.com");
    }

    @Test
    public void testPathComputation() throws Exception {
        RedirectFilter filter = new RedirectFilter() {

            @Override
            protected String getRedirectTo() {
                return "http://perdu.com";
            }

            @Override
            protected String getPrefix() {
                return "/proxy";
            }
        };

        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/proxy/foo/bar").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/proxy/foo/bar");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        URI uri = filter.rewriteURI(request);
        assertThat(uri.toString()).endsWith("/foo/bar");

    }

}