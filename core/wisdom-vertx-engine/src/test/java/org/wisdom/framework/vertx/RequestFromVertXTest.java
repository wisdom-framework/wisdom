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

import com.google.common.net.MediaType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpServerRequestImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.MimeTypes;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the behavior of the Request implementation.
 */
public class RequestFromVertXTest {

    Vertx vertx = Vertx.vertx();
    private ContextFromVertx context;

    @Before
    public void init() {
        context = mock(ContextFromVertx.class);
        when(context.vertx()).thenReturn(vertx);
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    /**
     * This is a hack to create HttpServerRequest. The constructor is not public, no, we call it using reflection.
     *
     * @param request the Netty request
     * @return the Http Server Request
     */
    public static HttpServerRequest create(HttpRequest request) {
        try {
            Class clazz =
                    RequestFromVertXTest.class.getClassLoader().loadClass("io.vertx.core.http.impl.ServerConnection");

            final Constructor<HttpServerRequestImpl> constructor = HttpServerRequestImpl.class
                    .getDeclaredConstructor(
                            clazz,
                            HttpRequest.class,
                            HttpServerResponse.class
                    );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    Mockito.mock(clazz),
                    request,
                    null
            );
        } catch (Exception e) {
            fail("Cannot create the Http Server Request", e);
        }
        return null;
    }

    @Test
    public void testContentType() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().add(HeaderNames.CONTENT_TYPE, MimeTypes.BINARY);
        RequestFromVertx request = new RequestFromVertx(create(req));

        assertThat(request.contentType()).isEqualTo(MimeTypes.BINARY);

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        request = new RequestFromVertx(create(req));
        assertThat(request.contentType()).isNull();
    }

    @Test
    public void testEncodingLanguageAndCharset() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().add(HeaderNames.ACCEPT_ENCODING, "gzip, deflate");
        req.headers().add(HeaderNames.ACCEPT_LANGUAGE, "en-US");
        req.headers().add(HeaderNames.ACCEPT_CHARSET, "utf-8");

        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.encoding()).isEqualTo("gzip, deflate");
        assertThat(request.language()).isEqualTo("en-US");
        assertThat(request.charset()).isEqualTo("utf-8");
    }

    @Test
    public void testUri() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.uri()).isEqualTo("/");
        assertThat(request.path()).isEqualTo("/");

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo");
        request = new RequestFromVertx(create(req));
        assertThat(request.uri()).isEqualTo("/foo");
        assertThat(request.path()).isEqualTo("/foo");

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo?k=v");
        request = new RequestFromVertx(create(req));
        assertThat(request.uri()).isEqualTo("/foo?k=v");
        assertThat(request.path()).isEqualTo("/foo");
    }

    @Test
    public void testMethod() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.method()).isEqualTo("GET");

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PATCH, "/");
        request = new RequestFromVertx(create(req));
        assertThat(request.method()).isEqualTo("PATCH");
    }

    @Test
    public void testRemoteAddress() throws Exception {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        when(ctx.channel()).thenReturn(channel);
        InetSocketAddress address = new InetSocketAddress("1.2.3.4", 1234);
        when(channel.remoteAddress()).thenReturn(address);

        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        // The case with the X_FORWARD_HEADER
        req.headers().add(HeaderNames.X_FORWARD_FOR, "localhost");
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.remoteAddress()).isEqualTo("localhost");
    }


    @Test
    public void testLanguageOrder() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().set(HeaderNames.ACCEPT_LANGUAGE, "da, en-gb;q=0.8, en;q=0.7");
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.languages()).containsExactly(
                new Locale("da"),
                new Locale("en", "gb"),
                new Locale("en")
        );

        req.headers().set(HeaderNames.ACCEPT_LANGUAGE, "da, en-gb;q=0.7, en;q=0.9");
        request = new RequestFromVertx(create(req));
        assertThat(request.languages()).containsExactly(
                new Locale("da"),
                new Locale("en"),
                new Locale("en", "gb")
        );
    }

    @Test
    public void testMediaType() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().set(HeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp," +
                "*/*;q=0.8");
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.mediaType().toString()).isEqualTo("text/html");

        req.headers().set(HeaderNames.ACCEPT, "application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        assertThat(request.mediaType().toString()).isEqualTo("application/xhtml+xml");

        req.headers().set(HeaderNames.ACCEPT, "application/xhtml+xml;q=0.1,application/xml;q=0.9;charset=utf-8,*/*;q=0.8");
        assertThat(request.mediaType().withoutParameters().toString()).isEqualTo("application/xml");

        req.headers().clear();
        assertThat(request.mediaType()).isEqualTo(MediaType.ANY_TEXT_TYPE);
        req.headers().set(HeaderNames.ACCEPT, "*/*");
        assertThat(request.mediaType()).isEqualTo(MediaType.ANY_TEXT_TYPE);

        req.headers().set(HeaderNames.ACCEPT, "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5");
        assertThat(request.mediaTypes()).containsExactly(
                MediaType.parse("text/html").withParameter("level", "1"),
                MediaType.parse("text/html").withParameter("q", "0.7"),
                MediaType.parse("*/*").withParameter("q", "0.5"),
                MediaType.parse("text/html").withParameter("level", "2").withParameter("q", "0.4"),
                MediaType.parse("text/*").withParameter("q", "0.3")
        );
    }

    @Test
    public void testAccepts() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().set(HeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp," +
                "*/*;q=0.8");
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.accepts("text/html")).isTrue();
        assertThat(request.accepts("application/xhtml+xml")).isTrue();
        assertThat(request.accepts("application/bla")).isFalse();
    }

    @Test
    public void testCookies() throws Exception {
        String c = "mediaWiki.user.id=0kn3VaEP7XG7mbxRPNgBOe5DNfOAGaHL; centralnotice_bucket=0-4.2; " +
                "uls-previous-languages=%5B%22en%22%5D; mediaWiki.user.sessionId=Mu2OplNdlL98mRoHEwKGlxYsOXbyP1f0; GeoIP=::::v6";
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().set(HeaderNames.COOKIE, c);
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.cookies().get("mediaWiki.user.id").value()).isEqualTo("0kn3VaEP7XG7mbxRPNgBOe5DNfOAGaHL");
        assertThat(request.cookies().get("GeoIP").value()).isEqualTo("::::v6");

        assertThat(request.cookie("mediaWiki.user.id").value()).isEqualTo("0kn3VaEP7XG7mbxRPNgBOe5DNfOAGaHL");
        assertThat(request.cookie("GeoIP").value()).isEqualTo("::::v6");

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        request = new RequestFromVertx(create(req));

        assertThat(request.cookies().get("GeoIP")).isNull();
        assertThat(request.cookie("GeoIP")).isNull();
    }

    @Test
    public void testHeaders() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        req.headers().add(HeaderNames.ACCEPT_ENCODING, "gzip, deflate");
        req.headers().add(HeaderNames.ACCEPT_LANGUAGE, "en-US");
        req.headers().add(HeaderNames.ACCEPT_CHARSET, "utf-8");
        req.headers().add("test", "a").add("test", "b");
        RequestFromVertx request = new RequestFromVertx(create(req));

        assertThat(request.headers().containsKey(HeaderNames.ACCEPT_LANGUAGE)).isTrue();
        assertThat(request.headers().get("test")).containsExactly("a", "b");
        assertThat(request.headers().get("missing")).isNull();
    }

    @Test
    public void testQueryParameter() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo?k=v&i=5&b=true");
        RequestFromVertx request = new RequestFromVertx(create(req));

        assertThat(request.parameter("k")).isEqualTo("v");
        assertThat(request.parameter("k", "v2")).isEqualTo("v");
        assertThat(request.parameter("none")).isNull();
        assertThat(request.parameter("none", "v2")).isEqualTo("v2");

        assertThat(request.parameterAsInteger("i")).isEqualTo(5);
        assertThat(request.parameterAsInteger("j")).isNull();
        assertThat(request.parameterAsInteger("i", 1)).isEqualTo(5);
        assertThat(request.parameterAsInteger("j", 1)).isEqualTo(1);

        assertThat(request.parameterAsBoolean("b")).isTrue();
        assertThat(request.parameterAsBoolean("b2")).isFalse();
        assertThat(request.parameterAsBoolean("b", false)).isTrue();
        assertThat(request.parameterAsBoolean("b2", true)).isTrue();

        req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo?k=v&i=5&b=true&i=6");
        request = new RequestFromVertx(create(req));
        assertThat(request.parameterMultipleValues("i")).containsExactly("5", "6");
    }

    @Test
    public void testParameterMultipleValues() throws Exception {
        HttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo?k=v&k=v2&k=v3");
        RequestFromVertx request = new RequestFromVertx(create(req));
        assertThat(request.parameterMultipleValues("k")).containsExactly("v", "v2", "v3");
    }
}
