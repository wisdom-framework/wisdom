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
package org.wisdom.framework.vertx;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.vertx.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContextFromVertxTest {

    Vertx vertx = Vertx.vertx();
    private ServiceAccessor accessor;
    private ApplicationConfiguration configuration;

    @Rule
    public RunOnVertxContext runOnVertxContext = new RunOnVertxContext();

    @Before
    public void setUp() {
        accessor = mock(ServiceAccessor.class);

        configuration = mock(ApplicationConfiguration.class);
        when(configuration.getWithDefault(Cookie.APPLICATION_COOKIE_PREFIX, "wisdom"))
            .thenReturn("wisdom");

        when(accessor.getConfiguration()).thenReturn(configuration);
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void testId() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor,
                RequestFromVertXTest.create(req));
        assertThat(context.id()).isNotNegative();
        long id1 = context.id();
        context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor,
                RequestFromVertXTest.create(req));
        assertThat(id1).isLessThan(context.id());
    }

    @Test
    public void testRequest() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor,
                RequestFromVertXTest.create(req));
        assertThat(context.request()).isNotNull();
    }

    @Test
    public void testPath() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor,
                RequestFromVertXTest.create(req));
        assertThat(context.path()).isEqualTo("/");

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo");

        context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor,
                RequestFromVertXTest.create(req));
        assertThat(context.path()).isEqualTo("/foo");

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo?k=v");
        context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor,
                RequestFromVertXTest.create(req));
        assertThat(context.path()).isEqualTo("/foo");
    }

    @Test
    public void testCookie() throws Exception {

        System.out.println(Thread.currentThread().getName() + " " + vertx.getOrCreateContext() + " " + Vertx.currentContext());

        String c = "mediaWiki.user.id=0kn3VaEP7XG7mbxRPNgBOe5DNfOAGaHL; centralnotice_bucket=0-4.2; " +
                "uls-previous-languages=%5B%22en%22%5D; mediaWiki.user.sessionId=Mu2OplNdlL98mRoHEwKGlxYsOXbyP1f0; GeoIP=::::v6";
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().set(HeaderNames.COOKIE, c);
        ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(), accessor, RequestFromVertXTest.create(req));
        assertThat(context.cookies().get("mediaWiki.user.id").value()).isEqualTo("0kn3VaEP7XG7mbxRPNgBOe5DNfOAGaHL");
        assertThat(context.cookies().get("GeoIP").value()).isEqualTo("::::v6");

        assertThat(context.cookie("mediaWiki.user.id").value()).isEqualTo("0kn3VaEP7XG7mbxRPNgBOe5DNfOAGaHL");
        assertThat(context.hasCookie("GeoIP")).isTrue();
        assertThat(context.cookie("GeoIP").value()).isEqualTo("::::v6");
        assertThat(context.cookieValue("GeoIP")).isEqualTo("::::v6");

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        context = new ContextFromVertx(vertx, vertx.getOrCreateContext(), accessor, RequestFromVertXTest.create(req));

        assertThat(context.cookies().get("GeoIP")).isNull();
        assertThat(context.cookie("GeoIP")).isNull();
        assertThat(context.hasCookie("GeoIP")).isFalse();
    }

    @Test
    public void testParameterFromQuery() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo?k=v&i=5&b=true");
        ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor, RequestFromVertXTest.create(req));

        assertThat(context.parameter("k")).isEqualTo("v");
        assertThat(context.parameter("k", "v2")).isEqualTo("v");
        assertThat(context.parameter("none")).isNull();
        assertThat(context.parameter("none", "v2")).isEqualTo("v2");

        assertThat(context.parameterAsInteger("i")).isEqualTo(5);
        assertThat(context.parameterAsInteger("j")).isNull();
        assertThat(context.parameterAsInteger("i", 1)).isEqualTo(5);
        assertThat(context.parameterAsInteger("j", 1)).isEqualTo(1);

        assertThat(context.parameterAsBoolean("b")).isTrue();
        assertThat(context.parameterAsBoolean("b2")).isFalse();
        assertThat(context.parameterAsBoolean("b", false)).isTrue();
        assertThat(context.parameterAsBoolean("b2", true)).isTrue();

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo?k=v&i=5&b=true&i=6");
        context = new ContextFromVertx(vertx,vertx.getOrCreateContext(),  accessor, RequestFromVertXTest.create(req));
        assertThat(context.parameterMultipleValues("i")).containsExactly("5", "6");
    }

    @Test
    public void testHeader() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().add(HeaderNames.ACCEPT_ENCODING, "gzip, deflate");
        req.headers().add(HeaderNames.ACCEPT_LANGUAGE, "en-US");
        req.headers().add(HeaderNames.ACCEPT_CHARSET, "utf-8");
        req.headers().add("test", "a").add("test", "b");
        ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor, RequestFromVertXTest.create(req));

        assertThat(context.headers().containsKey(HeaderNames.ACCEPT_LANGUAGE)).isTrue();
        assertThat(context.header(HeaderNames.ACCEPT_LANGUAGE)).isEqualTo("en-US");
        assertThat(context.headers().get("test")).containsExactly("a", "b");
        assertThat(context.headers("test")).containsExactly("a", "b");
        assertThat(context.headers().get("missing")).isNull();
        assertThat(context.header("missing")).isNull();
    }

    @Test
    public void testThatColonAreEncodedCorrectly() throws NoSuchMethodException {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
            "/foo?key=value:value");
        ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor,
            RequestFromVertXTest.create(request));
        assertThat(context.path()).isEqualToIgnoringCase("/foo");

        request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
            "/foo/bar:baz/x");
        context = new ContextFromVertx(vertx, vertx.getOrCreateContext(),  accessor, RequestFromVertXTest.create(request));
        Controller controller = new MyController();
        context.route(new Route(org.wisdom.api.http.HttpMethod.GET, "/foo/{p}/x",
            controller, MyController.class.getMethod("action")));
        assertThat(context.path()).isEqualToIgnoringCase("/foo/bar:baz/x");
        String p = context.parameterFromPath("p");
        assertThat(p).isEqualTo("bar:baz");
        p = context.parameterFromPathEncoded("p");
        assertThat(p).isEqualTo("bar:baz");

    }

    private class MyController extends DefaultController {

        public Result action() {
            return ok();
        }

    }
}