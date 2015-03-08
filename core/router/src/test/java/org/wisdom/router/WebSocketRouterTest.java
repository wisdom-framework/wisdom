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
package org.wisdom.router;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.api.http.websockets.WebSocketDispatcher;
import org.wisdom.content.converters.ParamConverterEngine;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.*;

/**
 * Check the WebSocket Router
 */
public class WebSocketRouterTest {


    @Test
    public void testRegistrationAndUnregistration() {
        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};

        router.bindDispatcher(dispatcher);
        verify(dispatcher, times(1)).register(router);

        router.stop();
        verify(dispatcher, times(1)).unregister(router);
    }

    @Test
    public void testControllerBindingWithNoAnnotation() {
        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};
        router.converter = new ParamConverterEngine(
                Collections.<ParameterConverter>emptyList(),
                Collections.<ParameterFactory>emptyList());

        final DefaultController controller = new DefaultController() {

            public void foo() {

            }

        };
        router.bindController(controller);

        assertThat(router.closes).isEmpty();
        assertThat(router.opens).isEmpty();
        assertThat(router.listeners).isEmpty();

        router.unbindController(controller);
    }

    String message;

    @Test
    public void testControllerWithOnMessageAnnotation() {
        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};
        router.executor = mock(ManagedExecutorService.class);
        router.converter = new ParamConverterEngine(
                Collections.<ParameterConverter>emptyList(),
                Collections.<ParameterFactory>emptyList());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Callable<Void>) invocation.getArguments()[0]).call();
                return null;
            }
        }).when(router.executor).submit(any(Callable.class));

        final DefaultController controller = new DefaultController() {

            @OnMessage("/ws")
            public void foo(@Body String message) {
                WebSocketRouterTest.this.message = message;
            }

        };
        router.bindController(controller);

        assertThat(router.closes).isEmpty();
        assertThat(router.opens).isEmpty();
        assertThat(router.listeners).hasSize(1);
        assertThat(router.listeners.iterator().next().check()).isTrue();
        assertThat(router.listeners.iterator().next().getController()).isEqualTo(controller);

        router.received("/ws", "client", "hello".getBytes(Charset.defaultCharset()));

        assertThat(message).isEqualTo("hello");

        router.unbindController(controller);
    }

    @Test
    public void testControllerWithOnMessageAnnotationWithParameters() {
        WebSocketRouter router = new WebSocketRouter();
        router.converter = new ParamConverterEngine(
                Collections.<ParameterConverter>emptyList(),
                Collections.<ParameterFactory>emptyList());
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};
        router.executor = mock(ManagedExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Callable<Void>) invocation.getArguments()[0]).call();
                return null;
            }
        }).when(router.executor).submit(any(Callable.class));

        final Map<String, String> results = new HashMap<>();
        final DefaultController controller = new DefaultController() {

            @OnMessage("/ws/{name}")
            public void foo(@Parameter("name") String name, @Body String message, @Parameter("client") String client) {
                results.put("message", message);
                results.put("name", name);
                results.put("client", client);
            }

        };
        router.bindController(controller);

        assertThat(router.closes).isEmpty();
        assertThat(router.opens).isEmpty();
        assertThat(router.listeners).hasSize(1);
        assertThat(router.listeners.iterator().next().check()).isTrue();
        assertThat(router.listeners.iterator().next().getController()).isEqualTo(controller);

        router.received("/ws/foo", "client", "hello".getBytes(Charset.defaultCharset()));

        assertThat(results)
                .contains(entry("message", "hello"))
                .contains(entry("client", "client"))
                .contains(entry("name", "foo"));

        results.clear();

        router.received("/ws", "client", "hello".getBytes(Charset.defaultCharset()));

        // Should not have been received.
        assertThat(results).isEmpty();

        router.unbindController(controller);
    }

    @Test
    public void testControllerWithOpenAndCloseAnnotations() {
        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};
        final Map<String, String> results = new HashMap<>();
        final DefaultController controller = new DefaultController() {

            @Opened("/ws")
            public void open() {
                results.put("open", "true");
            }

            @Opened("/ws")
            public void openWithClient(@Parameter("client") String client) {
                results.put("open-client", client);
            }

            @Closed("/ws")
            public void close() {
                results.put("close", "true");
            }

            @Closed("/ws")
            public void closeWithClient(@Parameter("client") String client) {
                results.put("close-client", client);
            }

        };
        router.bindController(controller);

        assertThat(router.closes).hasSize(2);
        assertThat(router.opens).hasSize(2);
        assertThat(router.listeners).hasSize(0);

        router.opened("/ws", "id");
        assertThat(results)
                .contains(entry("open", "true"))
                .contains(entry("open-client", "id"))
                .doesNotContainKey("close")
                .doesNotContainKey("close-client");

        router.closed("/ws", "id");
        assertThat(results)
                .contains(entry("open", "true"))
                .contains(entry("open-client", "id"))
                .contains(entry("close", "true"))
                .contains(entry("close-client", "id"));

        router.unbindController(controller);
    }

    @Test
    public void testSend() {
        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};

        router.send("/ws", "client", "hello");
        verify(dispatcher, times(1)).send("/ws", "client", "hello");

        router.send("/ws", "client", "hello".getBytes(Charset.defaultCharset()));
        verify(dispatcher, times(1)).send("/ws", "client", "hello".getBytes(Charset.defaultCharset()));

        router.send("/ws", "client", (String) null);
        // should not be sent.
        verify(dispatcher, times(0)).send("/ws", "client", (String) null);

        router.send("/ws", null, "hello");
        // should not be sent.
        verify(dispatcher, times(0)).send("/ws", null, "hello");

        router.send("/ws", "client", (byte[]) null);
        // should not be sent.
        verify(dispatcher, times(0)).send("/ws", "client", (byte[]) null);

        router.send("/ws", null, "hello".getBytes(Charset.defaultCharset()));
        // should not be sent.
        verify(dispatcher, times(0)).send("/ws", null, "hello".getBytes(Charset.defaultCharset()));
    }


    @Test
    public void testSendJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode object = mapper.createObjectNode();
        object.put("message", "hello");
        ArrayNode array = mapper.createArrayNode();
        array.add(object).add(mapper.createObjectNode().put("name", "wisdom"));

        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};

        router.send("/ws", "client", object);
        verify(dispatcher, times(1)).send("/ws", "client", object.toString());

        router.send("/ws", "client", array);
        verify(dispatcher, times(1)).send("/ws", "client", array.toString());

        router.send("/ws", "client", (JsonNode) null);
        // should not be sent.
        verify(dispatcher, times(1)).send("/ws", "client", NullNode.getInstance().toString());
    }

    @Test
    public void testPublish() {
        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};

        router.publish("/ws", "hello");
        verify(dispatcher, times(1)).publish("/ws", "hello");

        router.publish("/ws", "hello".getBytes(Charset.defaultCharset()));
        verify(dispatcher, times(1)).publish("/ws", "hello".getBytes(Charset.defaultCharset()));

        router.publish("/ws", (String) null);
        // should not be sent.
        verify(dispatcher, times(0)).publish("/ws", (String) null);

        router.publish("/ws", (byte[]) null);
        // should not be sent.
        verify(dispatcher, times(0)).publish("/ws", (byte[]) null);
    }

    @Test
    public void testPublishJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode object = mapper.createObjectNode();
        object.put("message", "hello");
        ArrayNode array = mapper.createArrayNode();
        array.add(object).add(mapper.createObjectNode().put("name", "wisdom"));

        WebSocketRouter router = new WebSocketRouter();
        final WebSocketDispatcher dispatcher = mock(WebSocketDispatcher.class);
        router.dispatchers = new WebSocketDispatcher[]{dispatcher};

        router.publish("/ws", object);
        verify(dispatcher, times(1)).publish("/ws", object.toString());

        router.publish("/ws", array);
        verify(dispatcher, times(1)).publish("/ws", array.toString());

        router.publish("/ws", (JsonNode) null);
        // should not be sent.
        verify(dispatcher, times(1)).publish("/ws", NullNode.getInstance().toString());
    }


}
