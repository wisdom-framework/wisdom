/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.framework.vertx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;
import org.wisdom.test.parents.FakeConfiguration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Check the behavior of the {@link Server}.
 */
public class ServerTest extends VertxBaseTest {

    private WisdomVertxServer wisdom;
    private ApplicationConfiguration application;
    private Router router;

    @Before
    public void setUp() {
        wisdom = new WisdomVertxServer();

        application = mock(ApplicationConfiguration.class);
        when(application.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(application.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(0);
        when(application.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(application.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(application.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(application.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(application.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);
        when(application.getBaseDir()).thenReturn(new File("target/junk/server/conf"));
        wisdom.configuration = application;
        wisdom.vertx = vertx;

        router = mock(Router.class);

        ContentEngine contentEngine = mock(ContentEngine.class);

        wisdom.accessor = new ServiceAccessor(
                null,
                application,
                router,
                contentEngine,
                null,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
    }

    @After
    public void tearDown() {
        if (wisdom != null) {
            wisdom.stop();
        }
    }


    @Test
    public void testCreationFromConfiguration() throws InterruptedException {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", true)
                .put("authentication", true)
                .build());


        when(application.getConfiguration("vertx.servers")).thenReturn(new FakeConfiguration(
                Collections.<String, Object>emptyMap()));

        Server server = Server.from(wisdom.accessor, vertx, "test", configuration);
        wisdom.servers.add(server);
        wisdom.start();
        waitForHttpsStart(wisdom);

        assertThat(server.port()).isNotEqualTo(-1).isNotEqualTo(0);
        assertThat(server.ssl()).isTrue();

        assertThat(server.accept("/foo")).isTrue();
    }

    /**
     * This methods checks HTTP, HTTPS and HTTPS with Mutual Authentication.
     */
    @Test
    public void testCreationOfThreeServersFromConfiguration() throws InterruptedException, IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException,
            KeyManagementException, UnrecoverableKeyException {
        FakeConfiguration s1 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", false)
                .put("authentication", false)
                .build());

        FakeConfiguration s2 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", true)
                .put("authentication", false)
                .build());

        FakeConfiguration s3 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", true)
                .put("authentication", true)
                .build());

        // Server HTTPS
        File root = new File("");
        final File serverKeyStore = new File(root.getAbsolutePath() + "/src/test/resources/keystore/server/server.jks");
        assertThat(serverKeyStore).isFile();
        when(application.get("https.keyStore")).thenReturn(
                serverKeyStore.getAbsolutePath());
        when(application.get("https.trustStore")).thenReturn(
                new File(root.getAbsolutePath() + "/src/test/resources/keystore/server/server.jks").getAbsolutePath());
        when(application.getWithDefault("https.keyStoreType", "JKS")).thenReturn("JKS");
        when(application.getWithDefault("https.trustStoreType", "JKS")).thenReturn("JKS");
        when(application.getWithDefault("https.keyStorePassword", "")).thenReturn("wisdom");
        when(application.getWithDefault("https.trustStorePassword", "")).thenReturn("wisdom");

        when(application.getWithDefault("https.keyStoreAlgorithm", KeyManagerFactory.getDefaultAlgorithm()))
                .thenReturn(KeyManagerFactory.getDefaultAlgorithm());
        when(application.getWithDefault("https.trustStoreAlgorithm", KeyManagerFactory.getDefaultAlgorithm()))
                .thenReturn(KeyManagerFactory.getDefaultAlgorithm());
        when(application.getConfiguration("vertx.servers")).thenReturn(
                new FakeConfiguration(
                        ImmutableMap.<String, Object>of(
                                "s1", s1,
                                "s2", s2,
                                "s3", s3
                        )
                ));

        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok("Alright");
            }
        };
        Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        wisdom.start();
        waitForStart(wisdom);
        waitForHttpsStart(wisdom);

        assertThat(wisdom.servers).hasSize(3);

        // Check rendering
        for (Server server : wisdom.servers) {
            String r;
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream instream = new FileInputStream("src/test/resources/keystore/client/client1.jks");
            trustStore.load(instream, "wisdom".toCharArray());

            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                    .loadKeyMaterial(trustStore, "wisdom".toCharArray())
                    .build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{"TLSv1", "SSLv3"},
                    null,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();

            if (server.ssl()) {
                HttpGet httpget = new HttpGet("https://localhost:" + server.port());
                final CloseableHttpResponse response = httpclient.execute(httpget);
                r = EntityUtils.toString(response.getEntity());
            } else {
                r = org.apache.http.client.fluent.Request
                        .Get("http://localhost:" + server.port()).execute().returnContent().asString();
            }

            assertThat(r).isEqualToIgnoringCase("Alright");
        }
    }

    @Test
    public void testAllow() throws InterruptedException, IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException,
            KeyManagementException, UnrecoverableKeyException {
        FakeConfiguration s1 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", false)
                .put("authentication", false)
                .put("allow", ImmutableList.of("/foo*"))
                .build());

        FakeConfiguration s2 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", false)
                .put("authentication", false)
                .put("allow", ImmutableList.of("/bar*"))
                .build());

        when(application.getConfiguration("vertx.servers")).thenReturn(
                new FakeConfiguration(
                        ImmutableMap.<String, Object>of(
                                "s1", s1,
                                "s2", s2
                        )
                ));
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok("Alright");
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/foo")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/bar")
                .to(controller, "index");
        doAnswer(new Answer<Route>() {
            @Override
            public Route answer(InvocationOnMock mock) throws Throwable {
                String url = (String) mock.getArguments()[1];
                if (url.equals("/foo")) {
                    return route1;
                }
                if (url.equals("/bar")) {
                    return route2;
                }
                return null;
            }
        }).when(router).getRouteFor(anyString(), anyString(), any(Request.class));

        wisdom.start();
        waitForStart(wisdom);

        assertThat(wisdom.servers).hasSize(2);
        for (Server server : wisdom.servers) {
            if (server.name().equalsIgnoreCase("s1")) {
                // Accept /foo, Deny /bar
                HttpResponse r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/foo").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.OK);
                EntityUtils.consumeQuietly(r.getEntity());
                r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/bar").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.FORBIDDEN);
                EntityUtils.consumeQuietly(r.getEntity());
            } else {
                // Accept /foo, Deny /bar
                HttpResponse r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/bar").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.OK);
                EntityUtils.consumeQuietly(r.getEntity());
                r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/foo").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.FORBIDDEN);
                EntityUtils.consumeQuietly(r.getEntity());
            }
        }
    }

    @Test
    public void testDeny() throws InterruptedException, IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException,
            KeyManagementException, UnrecoverableKeyException {
        FakeConfiguration s1 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", false)
                .put("authentication", false)
                .put("deny", ImmutableList.of("/bar*"))
                .build());

        FakeConfiguration s2 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", false)
                .put("authentication", false)
                .put("deny", ImmutableList.of("/foo*"))
                .build());

        when(application.getConfiguration("vertx.servers")).thenReturn(
                new FakeConfiguration(
                        ImmutableMap.<String, Object>of(
                                "s1", s1,
                                "s2", s2
                        )
                ));

        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok("Alright");
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/foo")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/bar")
                .to(controller, "index");
        doAnswer(new Answer<Route>() {
            @Override
            public Route answer(InvocationOnMock mock) throws Throwable {
                String url = (String) mock.getArguments()[1];
                if (url.equals("/foo")) {
                    return route1;
                }
                if (url.equals("/bar")) {
                    return route2;
                }
                return null;
            }
        }).when(router).getRouteFor(anyString(), anyString(), any(Request.class));

        wisdom.start();
        waitForStart(wisdom);

        assertThat(wisdom.servers).hasSize(2);
        for (Server server : wisdom.servers) {
            if (server.name().equalsIgnoreCase("s1")) {
                // Accept /foo, Deny /bar
                HttpResponse r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/foo").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.OK);
                EntityUtils.consumeQuietly(r.getEntity());
                r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/bar").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.FORBIDDEN);
                EntityUtils.consumeQuietly(r.getEntity());
            } else {
                // Accept /foo, Deny /bar
                HttpResponse r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/bar").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.OK);
                EntityUtils.consumeQuietly(r.getEntity());
                r = org.apache.http.client.fluent.Request.Get(
                        "http://localhost:" + server.port() + "/foo").execute().returnResponse();
                assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.FORBIDDEN);
                EntityUtils.consumeQuietly(r.getEntity());
            }
        }
    }


    @Test
    public void testDenyWithRedirect() throws InterruptedException, IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException,
            KeyManagementException, UnrecoverableKeyException {
        FakeConfiguration s1 = new FakeConfiguration(ImmutableMap.<String, Object>builder()
                .put("port", 0)
                .put("ssl", false)
                .put("authentication", false)
                .put("deny", ImmutableList.of("/bar*"))
                .put("onDenied", "/foo")
                .build());

        when(application.getConfiguration("vertx.servers")).thenReturn(
                new FakeConfiguration(
                        ImmutableMap.<String, Object>of(
                                "s1", s1
                        )
                ));

        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok("Alright");
            }

            @SuppressWarnings("unused")
            public Result bar() {
                return ok("bar");
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/foo")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/bar")
                .to(controller, "bar");
        doAnswer(new Answer<Route>() {
            @Override
            public Route answer(InvocationOnMock mock) throws Throwable {
                String url = (String) mock.getArguments()[1];
                if (url.equals("/foo")) {
                    return route1;
                }
                if (url.equals("/bar")) {
                    return route2;
                }
                return null;
            }
        }).when(router).getRouteFor(anyString(), anyString(), any(Request.class));

        wisdom.start();
        waitForStart(wisdom);

        assertThat(wisdom.servers).hasSize(1);
        for (Server server : wisdom.servers) {
            // foo allowed
            HttpResponse r = org.apache.http.client.fluent.Request.Get(
                    "http://localhost:" + server.port() + "/foo").execute().returnResponse();
            assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.OK);

            // bar denied => redirected
            r = org.apache.http.client.fluent.Request.Get(
                    "http://localhost:" + server.port() + "/bar").execute().returnResponse();
            assertThat(r.getStatusLine().getStatusCode()).isEqualTo(Status.OK);
            assertThat(EntityUtils.toString(r.getEntity())).contains("Alright");
        }
    }

}