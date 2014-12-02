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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.HttpHeaders;
import org.junit.Test;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.framework.filters.BalancerFilter;
import org.wisdom.framework.filters.BalancerMember;
import org.wisdom.framework.filters.DefaultBalancerMember;
import org.wisdom.test.parents.FakeContext;
import org.wisdom.test.parents.FakeRequest;
import org.wisdom.test.parents.WisdomUnitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BalancerFilterTest extends WisdomUnitTest {


    @Test
    public void testRoundRobin() throws Exception {
        BalancerMember member1 = new DefaultBalancerMember("member-1", "http://perdu.com", "balancer");
        BalancerMember member2 = new DefaultBalancerMember("member-2", "http://perdus.com", "balancer");

        BalancerFilter balancer = new BalancerFilter() {

            @Override
            public String getName() {
                return "balancer";
            }

            @Override
            protected boolean followRedirect(String method) {
                return true;
            }
        };

        balancer.addMember(member1);
        balancer.addMember(member2);

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Vous Etes Perdu");

        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Perdus sur Internet");

        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Vous Etes Perdu");

        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Perdus sur Internet");
    }

    @Test
    public void testMemberDynamism() throws Exception {
        BalancerMember member1 = new DefaultBalancerMember("member-1", "http://perdu.com", "balancer");
        BalancerMember member2 = new DefaultBalancerMember("member-2", "http://perdus.com", "balancer");

        BalancerFilter balancer = new BalancerFilter() {

            @Override
            public String getName() {
                return "balancer";
            }

            @Override
            protected boolean followRedirect(String method) {
                return true;
            }
        };

        balancer.addMember(member1);
        balancer.addMember(member2);

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Vous Etes Perdu");

        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Perdus sur Internet");

        // Remove member1
        balancer.removeMember(member1);

        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Perdus sur Internet");

        //Re-add member 1
        balancer.addMember(member1);

        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Vous Etes Perdu");

    }

    @Test
    public void testStickySession() throws Exception {
        BalancerMember member1 = new DefaultBalancerMember("member-1", "http://perdu.com", "balancer");
        BalancerMember member2 = new DefaultBalancerMember("member-2", "http://perdus.com", "balancer");

        BalancerFilter balancer = new BalancerFilter() {

            @Override
            public String getName() {
                return "balancer";
            }

            @Override
            public boolean getStickySession() {
                return true;
            }

            @Override
            protected boolean followRedirect(String method) {
                return true;
            }
        };

        balancer.addMember(member1);
        balancer.addMember(member2);

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive").setParameter("_balancer", "member-2");

        FakeRequest request = new FakeRequest(context)
                .method(HttpMethod.GET)
                .uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Perdus sur Internet");

        context.setParameter("_balancer", "member-1");
        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Vous Etes Perdu");
    }

    @Test
    public void testFallbackOnStickySession() throws Exception {
        BalancerMember member1 = new DefaultBalancerMember("member-1", "http://perdu.com", "balancer");
        BalancerMember member2 = new DefaultBalancerMember("member-2", "http://perdus.com", "balancer");

        BalancerFilter balancer = new BalancerFilter() {

            @Override
            public String getName() {
                return "balancer";
            }

            @Override
            public boolean getStickySession() {
                return true;
            }

            @Override
            protected boolean followRedirect(String method) {
                return true;
            }
        };

        balancer.addMember(member1);
        balancer.addMember(member2);

        Route route = mock(Route.class);
        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive").setParameter("_balancer", "member-2");

        FakeRequest request = new FakeRequest(context)
                .method(HttpMethod.GET)
                .uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        Result result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Perdus sur Internet");

        context.setParameter("_balancer", "member-1");
        request = new FakeRequest(context).method(HttpMethod.GET).uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Vous Etes Perdu");

        // remove member - 2
        balancer.removeMember(member2);

        context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive")
                .setParameter("_balancer", "member-2");

        request = new FakeRequest(context)
                .method(HttpMethod.GET)
                .uri("/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);
        result = ((AsyncResult) balancer.call(route, rc)).callable().call();

        // Fallback to member-1
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(streamToString(result)).contains("Vous Etes Perdu");

    }

    @Test
    public void testBalancerConfiguration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("prefix")).thenReturn("/proxy");
        when(configuration.getOrDie("name")).thenReturn("balancer");
        when(configuration.getBooleanWithDefault("stickySession", false))
                .thenReturn(true);
        when(configuration.getBooleanWithDefault("proxyPassReverse", false))
                .thenReturn(true);

        BalancerFilter balancer = new BalancerFilter(configuration);
        assertThat(balancer.getName()).isEqualTo("balancer");
        assertThat(balancer.getProxyPassReverse()).isTrue();
        assertThat(balancer.getStickySession()).isTrue();
    }

    @Test
    public void testReverseRoutingHeaderModification() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("prefix")).thenReturn("/app");
        when(configuration.getOrDie("name")).thenReturn("balancer");
        when(configuration.getBooleanWithDefault("stickySession", false))
                .thenReturn(true);
        when(configuration.getBooleanWithDefault("proxyPassReverse", false))
                .thenReturn(true);
        BalancerFilter balancer = new BalancerFilter(configuration);

        BalancerMember member1 = new DefaultBalancerMember("member-1", "http://foo.com",
                "balancer");

        balancer.addMember(member1);

        RequestContext rc = mock(RequestContext.class);
        FakeContext context = new FakeContext();
        context.setPath("/").setHeader(HttpHeaders.CONNECTION, "keep-alive");

        FakeRequest request = new FakeRequest(context)
                .method(HttpMethod.GET)
                .uri("http://localhost:9000/app/");

        when(rc.context()).thenReturn(context);
        when(rc.request()).thenReturn(request);

        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put(HeaderNames.LOCATION, "http://foo.com/my/path?q=v#test");
        headers.put(HeaderNames.CONTENT_LOCATION, "http://foo.com/my/path");
        balancer.updateHeaders(rc, headers);

        assertThat(headers.entries()).hasSize(2);
        assertThat(headers.containsEntry(HeaderNames.LOCATION,
                "http://localhost:9000/my/path?q=v#test")).isTrue();
        assertThat(headers.containsEntry(HeaderNames.CONTENT_LOCATION,
                "http://localhost:9000/my/path")).isTrue();

    }

}