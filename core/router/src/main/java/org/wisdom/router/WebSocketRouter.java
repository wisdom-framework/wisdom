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
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Closed;
import org.wisdom.api.annotations.OnMessage;
import org.wisdom.api.annotations.Opened;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.http.websockets.WebSocketDispatcher;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.RouteUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Component handling web socket frame routing.
 */
@Component(immediate = true)
@Provides(specifications = Publisher.class)
@Instantiate(name = "WebSocketRouter")
public class WebSocketRouter implements WebSocketListener, Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketRouter.class);

    @Requires
    WebSocketDispatcher[] dispatchers;

    Set<DefaultWebSocketCallback> opens = new LinkedHashSet<>();
    Set<DefaultWebSocketCallback> closes = new LinkedHashSet<>();
    Set<OnMessageWebSocketCallback> listeners = new LinkedHashSet<>();

    @Requires(optional = true)
    private ContentEngine contentEngine;

    @Requires(optional = true)
    ParameterFactories converter;

    @Requires(filter = "(name=" + ManagedExecutorService.SYSTEM + ")")
    ManagedExecutorService executor;

    /**
     * @return the logger.
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Registers the current router in the dispatcher.
     */
    @Bind(aggregate = true)
    public void bindDispatcher(WebSocketDispatcher dispatcher) {
        dispatcher.register(this);
    }

    /**
     * Unregisters the current router in the dispatcher.
     */
    @Unbind
    public void unbindDispatcher(WebSocketDispatcher dispatcher) {
        dispatcher.unregister(this);
    }

    @Invalidate
    public void stop() {
        for (WebSocketDispatcher dispatcher : dispatchers) {
            dispatcher.unregister(this);
        }
    }

    /**
     * Binds a new controller.
     *
     * @param controller the new controller
     */
    @Bind(aggregate = true)
    public synchronized void bindController(Controller controller) {
        analyze(controller);
    }

    /**
     * @return the parameter converter.
     */
    public ParameterFactories converter() {
        return converter;
    }

    /**
     * @return the content engine.
     */
    public ContentEngine engine() {
        return contentEngine;
    }

    /**
     * Extracts all the annotations from the controller's method.
     *
     * @param controller the controller to analyze
     */
    private void analyze(Controller controller) {
        String prefix = RouteUtils.getPath(controller);
        Method[] methods = controller.getClass().getMethods();
        for (Method method : methods) {
            Opened open = method.getAnnotation(Opened.class);
            Closed close = method.getAnnotation(Closed.class);
            OnMessage on = method.getAnnotation(OnMessage.class);
            if (open != null) {

                DefaultWebSocketCallback callback = new DefaultWebSocketCallback(controller, method,
                        RouteUtils.getPrefixedUri(prefix, open.value()), this);
                if (callback.check()) {
                    opens.add(callback);
                }
            }
            if (close != null) {
                DefaultWebSocketCallback callback = new DefaultWebSocketCallback(controller, method,
                        RouteUtils.getPrefixedUri(prefix, close.value()), this);
                if (callback.check()) {
                    closes.add(callback);
                }
            }
            if (on != null) {
                OnMessageWebSocketCallback callback = new OnMessageWebSocketCallback(controller, method,
                        RouteUtils.getPrefixedUri(prefix, on.value()), this);
                if (callback.check()) {
                    listeners.add(callback);
                }
            }
        }

    }

    /**
     * Unbinds the controller.
     *
     * @param controller the leaving controller
     */
    @Unbind
    public synchronized void unbindController(Controller controller) {
        List<DefaultWebSocketCallback> toRemove = new ArrayList<>();
        for (DefaultWebSocketCallback open : opens) {
            if (open.getController() == controller) {
                toRemove.add(open);
            }
        }
        opens.removeAll(toRemove);

        toRemove.clear();
        for (DefaultWebSocketCallback close : closes) {
            if (close.getController() == controller) {
                toRemove.add(close);
            }
        }
        closes.removeAll(toRemove);

        toRemove.clear();
        for (DefaultWebSocketCallback callback : listeners) {
            if (callback.getController() == controller) {
                toRemove.add(callback);
            }
        }
        listeners.removeAll(toRemove);  //NOSONAR type is correct here.
    }

    /**
     * Handles the reception of a message.
     *
     * @param uri     the url of the web socket
     * @param from    the client having sent the message (octal id).
     * @param content the received content
     */
    @Override
    public void received(final String uri, final String from, final byte[] content) {
        for (final OnMessageWebSocketCallback listener : listeners) {
            if (listener.matches(uri)) {
                 executor.submit(new Callable<Void>() {
                     @Override
                     public Void call() throws Exception {
                         try {
                             listener.invoke(uri, from, content);
                         } catch (InvocationTargetException e) { //NOSONAR
                             LOGGER.error("An error occurred in the @OnMessage callback {}#{} : {}",
                                     listener.getController().getClass().getName(), listener.getMethod().getName
                                             (), e.getTargetException().getMessage(), e.getTargetException()
                             );
                         } catch (Exception e) {
                             LOGGER.error("An error occurred in the @OnMessage callback {}#{} : {}",
                                     listener.getController().getClass().getName(), listener.getMethod().getName(), e.getMessage(), e);
                         }
                         return null;
                     }
                 });
            }
        }
    }

    /**
     * Handles the registration of a new client on the web socket.
     *
     * @param uri    the url of the web socket
     * @param client the client id
     */
    @Override
    public void opened(String uri, String client) {
        for (DefaultWebSocketCallback open : opens) {
            if (open.matches(uri)) {
                try {
                    open.invoke(uri, client, null);
                } catch (InvocationTargetException e) { //NOSONAR
                    LOGGER.error("An error occurred in the @Open callback {}#{} : {}",
                            open.getController().getClass().getName(), open.getMethod().getName
                                    (), e.getTargetException().getMessage(), e.getTargetException()
                    );
                } catch (Exception e) {
                    LOGGER.error("An error occurred in the @Open callback {}#{} : {}",
                            open.getController().getClass().getName(), open.getMethod().getName(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Handles the disconnection of a client from the web socket.
     *
     * @param uri    the url of the web socket
     * @param client the client id
     */
    @Override
    public void closed(String uri, String client) {
        for (DefaultWebSocketCallback close : closes) {
            if (close.matches(uri)) {
                try {
                    close.invoke(uri, client, null);
                } catch (InvocationTargetException e) { //NOSONAR
                    LOGGER.error("An error occurred in the @Close callback {}#{} : {}",
                            close.getController().getClass().getName(), close.getMethod().getName
                                    (), e.getTargetException().getMessage(), e.getTargetException()
                    );
                } catch (Exception e) {
                    LOGGER.error("An error occurred in the @Close callback {}#{} : {}",
                            close.getController().getClass().getName(), close.getMethod().getName(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Publishes a text message on the web socket.
     * All client's connected to the given websocket receive the message.
     *
     * @param uri     the websocket's url
     * @param message the message (text)
     */
    @Override
    public void publish(String uri, String message) {
        if (message == null) {
            LOGGER.warn("Cannot send websocket message on {}, the message is null", uri);
            return;
        }
        for (WebSocketDispatcher dispatcher : dispatchers) {
            dispatcher.publish(uri, message);
        }
    }

    /**
     * Publishes a binary message on the web socket.
     * All client's connected to the given websocket receive the message.
     *
     * @param uri     the websocket's url
     * @param message the data (binary)
     */
    @Override
    public void publish(String uri, byte[] message) {
        if (message == null) {
            LOGGER.warn("Cannot send websocket message on {}, the message is null", uri);
            return;
        }
        for (WebSocketDispatcher dispatcher : dispatchers) {
            dispatcher.publish(uri, message);
        }
    }

    /**
     * Publishes a JSON message on the web socket.
     * All client's connected to the given websocket receive the message.
     *
     * @param uri     the websocket's url
     * @param message the data (JSON)
     */
    @Override
    public void publish(String uri, JsonNode message) {
        for (WebSocketDispatcher dispatcher : dispatchers) {
            if (message == null) {
                dispatcher.publish(uri, NullNode.getInstance().toString());
            } else {
                dispatcher.publish(uri, message.toString());
            }
        }
    }

    /**
     * Sends the given text message to the identified client. If the client is not connected on the web socket,
     * nothing happens.
     *
     * @param uri     the websocket's url
     * @param client  the client that is going to receive the message
     * @param message the data (text)
     */
    @Override
    public void send(String uri, String client, String message) {
        if (message == null || client == null) {
            LOGGER.warn("Cannot send websocket message on {}, either the client id is null ({}) of the message is " +
                    "null ({})", uri, client, message);
            return;
        }
        for (WebSocketDispatcher dispatcher : dispatchers) {
            dispatcher.send(uri, client, message);
        }
    }

    /**
     * Sends the given json message to the identified client. If the client is not connected on the web socket,
     * nothing happens.
     *
     * @param uri     the websocket's url
     * @param client  the client that is going to receive the message
     * @param message the data (json)
     */
    @Override
    public void send(String uri, String client, JsonNode message) {
        for (WebSocketDispatcher dispatcher : dispatchers) {
            if (message == null) {
                dispatcher.send(uri, client, NullNode.getInstance().toString());
            } else {
                dispatcher.send(uri, client, message.toString());
            }
        }
    }

    /**
     * Sends the given binary message to the identified client. If the client is not connected on the web socket,
     * nothing happens.
     *
     * @param uri     the websocket's url
     * @param client  the client that is going to receive the message
     * @param message the data (binary)
     */
    @Override
    public void send(String uri, String client, byte[] message) {
        if (message == null || client == null) {
            LOGGER.warn("Cannot send websocket message on {}, either the client id is null ({}) of the message is " +
                    "null ({})", uri, client, message);
            return;
        }
        for (WebSocketDispatcher dispatcher : dispatchers) {
            dispatcher.send(uri, client, message);
        }
    }
}
