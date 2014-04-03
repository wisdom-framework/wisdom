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
package org.wisdom.wamp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.Json;
import org.wisdom.api.engine.WisdomEngine;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.wamp.messages.*;
import org.wisdom.wamp.services.ExportedService;
import org.wisdom.wamp.services.RegistryException;
import org.wisdom.wamp.services.Wamp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.wisdom.api.Controller;

/**
 * The WAMP main controller.
 * This controller is responsible for receiving the WAMP messages and sending the responses.
 * Components willing to be exposed using the Wamp protocol must register themselves using the WampRegistry service.
 */
@Component(immediate = true)
@Provides(specifications = {Wamp.class, EventHandler.class, Controller.class})
@Instantiate
public class WampController extends DefaultController implements Wamp, EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WampController.class);

    private final String prefix;
    private final String errorPrefix;

    @Requires
    Publisher publisher;

    @Requires
    Json json;

    @Requires(optional = true)
    EventAdmin ea;

    /**
     * The topics from the event admin listened by this component to transfer the event to WAMP.
     */
    @ServiceProperty(name = EventConstants.EVENT_TOPIC)
    String[] topics = new String[]{
            "wamp/*"
    };

    /**
     * To avoid loop, we ignore all event send by us. The event we sent have a 'wamp.topic' property,
     * to we accept only event without this property.
     */
    @ServiceProperty(name = EventConstants.EVENT_FILTER)
    String filter = "(!(" + WAMP_TOPIC_EVENT_PROPERTY + "=*))";

    private Map<String, WampClient> clients = Collections.unmodifiableMap(new HashMap<String, WampClient>());

    private Map<String, ExportedService> registry = Collections.unmodifiableMap(new HashMap<String, ExportedService>());

    @SuppressWarnings("UnusedDeclaration")
    public WampController(@Requires WisdomEngine engine) {
        String urlPrefix = "http://" + engine.hostname();
        if (engine.httpPort() != 80) {
            urlPrefix += ":" + engine.httpPort();
        }
        prefix = urlPrefix + Constants.WAMP_ROUTE;
        errorPrefix = prefix + Constants.WAMP_ERROR;
    }

    /**
     * Constructor used for testing purpose only.
     * Do not use directly outside the test scope.
     *
     * @param json      the json service
     * @param publisher the publisher
     * @param prefix    the  url prefix
     */
    public WampController(Json json, Publisher publisher, String prefix) {
        this.json = json;
        this.publisher = publisher;
        this.prefix = prefix;
        this.errorPrefix = prefix + Constants.WAMP_ERROR;
    }

    @Opened(Constants.WAMP_ROUTE)
    public void open(@Parameter("client") String id) {
        LOGGER.info("Opening Wamp connection for client " + id);
        WampClient client = addClient(id);
        if (client != null) {
            // Send the welcome message.
            sendOnWebSocket(new Welcome(client.session()).toJson(json), client);
        }
    }

    @Closed(Constants.WAMP_ROUTE)
    public void close(@Parameter("client") String id) {
        removeClient(id);
    }

    @OnMessage(Constants.WAMP_ROUTE)
    public void onMessage(@Parameter("client") String id, @Body ArrayNode message) {
        MessageType type = MessageType.getType(message.get(0).asInt());
        Map.Entry<String, WampClient> entry = getClientById(id);

        switch (type) {
            case PREFIX:
                handlePrefixMessage(id, entry, message);
                break;
            case CALL:
                handleCallMessage(id, entry, message);
                break;
            case SUBSCRIBE:
                handleSubscription(id, entry, message);
                break;
            case UNSUBSCRIBE:
                handleUnsubscription(id, entry, message);
                break;
            case PUBLISH:
                handlePublication(id, entry, message);
                break;
            default:
                LOGGER.error("Illegal WAMP message type code {}", type.code());
                break;
        }
    }

    @Invalidate
    public void stop() {
        clients = Collections.unmodifiableMap(new HashMap<String, WampClient>());
        registry = Collections.unmodifiableMap(new HashMap<String, ExportedService>());
    }

    private void handleCallMessage(String id, Map.Entry<String, WampClient> entry, ArrayNode message) {
        if (entry == null) {
            LOGGER.error("Invalid CALL message, cannot identify the client {}", id);
            return;
        }
        if (message.get(1) == null || message.get(1).asText() == null) {
            LOGGER.error("Invalid CALL message, callId not defined in the CALL message {}", message.toString());
            sendOnWebSocket(
                    new RPCError("0", new IllegalArgumentException("callId not defined in CALL message"),
                            errorPrefix).toJson(json),
                    entry.getValue()
            );
            return;
        }
        String callId = message.get(1).asText();

        if (message.get(2) == null || message.get(2).asText() == null) {
            LOGGER.error("Invalid CALL message, procId not defined in the CALL message {}", message.toString());
            sendOnWebSocket(
                    new RPCError(callId, new IllegalArgumentException("procId not defined in CALL message"),
                            errorPrefix).toJson(json),
                    entry.getValue()
            );
            return;
        }
        String procId = message.get(2).asText();

        List<JsonNode> args = new ArrayList<>();
        for (int i = 3; i < message.size(); i++) {
            args.add(message.get(i));
        }

        // Get full url from the potential compacted url (prefixed)
        String url = entry.getValue().getUri(procId);
        // Extract the service object identifier
        int index = url.indexOf("#");
        if (index == -1) {
            LOGGER.error("Invalid CALL message, malformed procId in the CALL message {}",
                    message.toString());
            sendOnWebSocket(
                    new RPCError(callId, new IllegalArgumentException("Malformed procId " + procId),
                            errorPrefix).toJson(json),
                    entry.getValue()
            );
            return;
        }
        String regId = url.substring(0, index);
        String method = url.substring(index + 1);

        ExportedService service;
        synchronized (this) {
            service = registry.get(regId);
        }
        if (service == null) {
            LOGGER.error("Invalid CALL message, cannot find service {} from message {}", regId, message.toString());
            sendOnWebSocket(
                    new RPCError(callId, new IllegalArgumentException("Service object " + regId + " not found"),
                            errorPrefix).toJson(json),
                    entry.getValue()
            );
            return;
        }
        if (method.isEmpty()) {
            LOGGER.error("Invalid CALL message, broken method name in message {}", message.toString());
            sendOnWebSocket(
                    new RPCError(callId, new IllegalArgumentException("Malformed method name in " + procId),
                            errorPrefix).toJson(json),
                    entry.getValue()
            );
            return;
        }

        // invocation
        handleRPCInvocation(entry.getValue(), callId, service, method, args);


    }

    private void handleRPCInvocation(WampClient client, String callId, ExportedService service, String method,
                                     List<JsonNode> args) {
        // Find method using reflection
        Method[] methods = service.service.getClass().getMethods();
        Method callback = null;
        for (Method m : methods) {
            if (m.getName().equals(method)) {
                callback = m;
                break;
            }
        }

        if (callback == null) {
            LOGGER.error("Invalid CALL message, cannot find method {} in class {}", method,
                    service.service.getClass().getName());
            sendOnWebSocket(
                    new RPCError(callId, new UnsupportedOperationException("Cannot find method " + method + " in " +
                            service.service.getClass().getName()),
                            errorPrefix
                    ).toJson(json),
                    client
            );
            return;
        }
        // IMPORTANT: method name must be unique.

        // Callback found, wrap arguments.
        Object[] arguments = new Object[callback.getParameterTypes().length];
        if (args.size() != arguments.length) {
            LOGGER.error("Invalid CALL message, the method {} exists in class {}, but the number of arguments does " +
                            "not match the RPC request", method,
                    service.service.getClass().getName()
            );
            sendOnWebSocket(
                    new RPCError(callId, new UnsupportedOperationException("Argument mismatch, " +
                            "expecting " + arguments.length + ", received " + args.size() + " values"),
                            errorPrefix
                    ).toJson(json),
                    client
            );
            return;
        }

        for (int i = 0; i < arguments.length; i++) {
            Class<?> type = callback.getParameterTypes()[i];
            JsonNode node = args.get(i);
            Object value = json.fromJson(node, type);
            arguments[i] = value;
        }

        // Invoke and send
        Object result;
        try {
            if (!callback.isAccessible()) {
                callback.setAccessible(true);
            }
            result = callback.invoke(service.service, arguments);
            RPCResult message = new RPCResult(callId, result);
            sendOnWebSocket(message.toJson(json), client);
        } catch (IllegalAccessException e) {
            LOGGER.error("Invalid CALL message, the method {} from class {} is not accessible", method,
                    service.service.getClass().getName(), e);
            sendOnWebSocket(
                    new RPCError(callId, e, "cannot access method " + callback.getName() + " from " + service
                            .service.getClass().getName(),
                            errorPrefix
                    ).toJson(json),
                    client
            );
        } catch (InvocationTargetException e) { //NOSONAR
            LOGGER.error("Invalid CALL message, the method {} from class {} has thrown an exception", method,
                    service.service.getClass().getName(), e.getCause());
            sendOnWebSocket(
                    new RPCError(callId, e.getTargetException(), "error while invoking " + callback.getName() + " " +
                            "from " + service.service.getClass().getName(),
                            errorPrefix
                    ).toJson(json),
                    client
            );
        }
    }

    private void handlePrefixMessage(String id, Map.Entry<String, WampClient> entry, ArrayNode message) {
        if (entry == null) {
            LOGGER.error("Invalid PREFIX message, cannot identify the client {}", id);
            return;
        }
        String prefix = message.get(1).asText();
        String uri = message.get(2).asText();
        entry.getValue().registerPrefix(prefix, uri);
    }

    private void handleSubscription(String id, Map.Entry<String, WampClient> entry, ArrayNode message) {
        if (entry == null) {
            LOGGER.error("Invalid SUBSCRIBE message, cannot identify the client {}", id);
            return;
        }
        if (message.get(1) == null || message.get(1).asText().isEmpty()) {
            // The subscription does not allow error reporting, just ignore.
            LOGGER.error("Invalid SUBSCRIBE message, missing topic in {}", message);
            return;
        }
        String uri = message.get(1).asText();
        entry.getValue().subscribe(uri);
    }

    private void handleUnsubscription(String id, Map.Entry<String, WampClient> entry, ArrayNode message) {
        if (entry == null) {
            LOGGER.error("Invalid UNSUBSCRIBE message, cannot identify the client {}", id);
            return;
        }
        if (message.get(1) == null || message.get(1).asText().isEmpty()) {
            // The un-subscription does not allow error reporting, just ignore.
            LOGGER.error("Invalid UNSUBSCRIBE message, missing topic in {}", message);
            return;
        }
        String uri = message.get(1).asText();
        entry.getValue().unsubscribe(uri);


        entry.getValue().unsubscribe(uri);
    }

    private void handlePublication(String id, Map.Entry<String, WampClient> entry, ArrayNode message) {
        if (entry == null) {
            LOGGER.error("Invalid PUBLISH message, cannot identify the client {}", id);
            return;
        }
        // The message parsing is a bit more complicated, as the argument type is important.
        if (message.get(1) == null || message.get(1).asText().isEmpty()) {
            // no error handling possible
            LOGGER.error("Invalid PUBLISH message, missing topic in {}", message);
            return;
        }
        String topic = message.get(1).asText();
        if (message.get(2) == null) {
            LOGGER.error("Invalid PUBLISH message, missing payload in {}", message);
            // no error handling possible
            return;
        }
        JsonNode event = message.get(2);

        List<String> exclusions = new ArrayList<>();
        if (message.get(3) != null) {
            // Two cases : boolean (excludeMe), or exclusion list
            if (message.get(3).isArray()) {
                ArrayNode array = (ArrayNode) message.get(3);
                for (JsonNode node : array) {
                    exclusions.add(node.asText());
                }
            } else if (message.get(3).isBoolean()) {
                if (message.get(3).asBoolean()) {
                    exclusions.add(entry.getValue().session());
                }
            } else {
                // Invalid format
                LOGGER.error("Invalid PUBLISH message, malformed message {} - the third argument must be either a " +
                        "boolean or an array", message);
                return;
            }
        }

        List<String> eligible = null;
        if (message.get(4) != null && message.get(4).isArray()) {
            eligible = new ArrayList<>();
            ArrayNode array = (ArrayNode) message.get(4);
            for (JsonNode node : array) {
                eligible.add(node.asText());
            }
        }

        dispatchEvent(topic, event, exclusions, eligible);
        sendOnEventAdmin(topic, event, exclusions, eligible);
    }

    private synchronized void dispatchEvent(String topic, JsonNode payload,
                                            List<String> exclusions, List<String> eligible) {
        Event event = new Event(topic, payload);
        for (WampClient c : clients.values()) {
            if (eligible != null) {
                // We must send the event to client from this list only
                if (eligible.contains(c.session())) {
                    sendOnWebSocket(event.toJson(json), c);
                }
            } else {
                if (exclusions == null || !exclusions.contains(c.session())) {
                    sendOnWebSocket(event.toJson(json), c);
                }
            }
        }
    }

    private void sendOnEventAdmin(String topic, JsonNode payload, List<String> exclusions, List<String> eligible) {
        Map<String, Object> map = new HashMap<>();
        map.put(WAMP_EVENT_PROPERTY, payload);
        if (exclusions != null) {
            map.put(WAMP_EXCLUSIONS_EVENT_PROPERTY, exclusions);
        }
        if (eligible != null) {
            map.put(WAMP_ELIGIBLE_EVENT_PROPERTY, exclusions);
        }
        map.put(WAMP_TOPIC_EVENT_PROPERTY, topic);
        org.osgi.service.event.Event event = new org.osgi.service.event.Event(getEventAdminTopicFromWampTopic(topic),
                map);
        ea.postEvent(event);
    }

    synchronized Map.Entry<String, WampClient> getClientById(String clientId) {
        for (Map.Entry<String, WampClient> entry : clients.entrySet()) {
            if (entry.getValue().wisdomClientId().equals(clientId)) {
                return entry;
            }
        }
        return null;
    }

    private synchronized WampClient addClient(String clientId) {
        Map.Entry<String, WampClient> entry = getClientById(clientId);
        if (entry != null) {
            // Existing client, return null.
            return null;
        }
        WampClient client = new WampClient(clientId);
        Map<String, WampClient> clientsNew = new HashMap<>();
        clientsNew.putAll(clients);
        clientsNew.put(client.session(), client);
        clients = Collections.unmodifiableMap(clientsNew);
        LOGGER.debug(client.session() + " connected.");
        return client;
    }

    private synchronized WampClient removeClient(String clientId) {
        Map.Entry<String, WampClient> entry = getClientById(clientId);
        if (entry == null) {
            return null;
        }
        Map<String, WampClient> clientsNew = new HashMap<>();
        clientsNew.putAll(clients);
        clientsNew.remove(entry.getKey());
        clients = Collections.unmodifiableMap(clientsNew);
        LOGGER.debug(entry.getKey() + " disconnected.");
        return entry.getValue();
    }

    @Override
    public ExportedService register(Object service, String url) throws RegistryException {
        return register(service, null, url);
    }

    @Override
    public ExportedService register(Object service, Map<String, Object> properties, String url) throws RegistryException {
        ExportedService svc;

        if (!url.startsWith("http://")) {
            if (url.startsWith("/")) {
                url = getWampBaseUrl() + url;
            } else {
                url = getWampBaseUrl() + "/" + url;
            }
        }

        synchronized (this) {
            if (registry.containsKey(url)) {
                throw new RegistryException("Cannot register service on url " + url + " - url already taken");
            }
            svc = new ExportedService(service, properties, url);
            HashMap<String, ExportedService> newRegistry = new HashMap<>(registry);
            newRegistry.put(url, svc);
            registry = Collections.unmodifiableMap(newRegistry);
        }
        return svc;
    }

    @Override
    public void unregister(String url) {
        if (!url.startsWith("http://")) {
            if (url.startsWith("/")) {
                url = getWampBaseUrl() + url;
            } else {
                url = getWampBaseUrl() + "/" + url;
            }
        }

        synchronized (this) {
            if (registry.containsKey(url)) {
                HashMap<String, ExportedService> newRegistry = new HashMap<>(registry);
                newRegistry.remove(url);
                registry = Collections.unmodifiableMap(newRegistry);
            }
        }
    }

    @Override
    public void unregister(ExportedService svc) {
        unregister(svc.url);
    }

    @Override
    public synchronized Collection<ExportedService> getServices() {
        return registry.values();
    }

    /**
     * Gets the WAMP base url.
     *
     * @return the WAMP base url.
     */
    @Override
    public String getWampBaseUrl() {
        return prefix;
    }

    /**
     * Transforms the given Event Admin topic to the WAMP topic.
     * Notice that topic should start with 'wamp/', if not 'wamp/' is added.
     * For example:
     * wamp/org/example is mapped to http://host:port/wamp/org/example
     * org/example is mapped to http://host:port/wamp/org/example
     *
     * @param topic the topic from the event admin
     * @return the associated WAMP topic
     */
    @Override
    public String getWampTopicFromEventAdminTopic(String topic) {
        final String topicPrefix = "wamp/";
        if (topic.startsWith(topicPrefix)) {
            return prefix + "/" + topic.substring(topicPrefix.length());
        } else {
            return prefix + "/" + topic;
        }
    }

    /**
     * Transforms a topic from WAMP to an Event Admin topic.
     * For example, the topic http://host:port/wamp/org/example is mapped to org/example.
     *
     * @param topic the topic from WAMP
     * @return the associated Event Admin topic
     */
    @Override
    public String getEventAdminTopicFromWampTopic(String topic) {
        // The +1 removes the trailing /
        return topic.substring(prefix.length() + 1);
    }


    /**
     * Sends the given message on the websocket.
     *
     * @param message the message
     * @param to      the client receiving the message.
     */
    private void sendOnWebSocket(JsonNode message, WampClient to) {
        publisher.send(Constants.WAMP_ROUTE, to.wisdomClientId(), message.toString());
    }

    /**
     * Called by the {@link org.osgi.service.event.EventAdmin} service to notify the listener of an
     * event.
     *
     * @param event The event that occurred.
     */
    @Override
    public void handleEvent(org.osgi.service.event.Event event) {
        String topic = getWampTopicFromEventAdminTopic(event.getTopic());
        Map<String, Object> map = new HashMap<>();
        List<String> eligible = null;
        List<String> exclusions = null;
        for (String name : event.getPropertyNames()) {
            if (WAMP_ELIGIBLE_EVENT_PROPERTY.equals(name)) {
                eligible = (List<String>) event.getProperty(name);
            } else if (WAMP_EXCLUSIONS_EVENT_PROPERTY.equals(name)) {
                exclusions = (List<String>) event.getProperty(name);
            } else {
                map.put(name, event.getProperty(name));
            }
        }
        JsonNode payload = json.toJson(map);
        dispatchEvent(topic, payload, exclusions, eligible);
    }
}
