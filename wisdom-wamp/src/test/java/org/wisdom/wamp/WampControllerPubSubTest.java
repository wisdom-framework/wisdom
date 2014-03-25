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
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.event.EventAdmin;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.wamp.data.Struct;
import org.wisdom.wamp.messages.MessageType;
import org.wisdom.wamp.services.RegistryException;
import org.wisdom.wamp.utils.JsonService;
import org.wisdom.wamp.utils.TestConstants;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Check the behavior of the Pub / Sub logic of the WampController
 */
public class WampControllerPubSubTest {

    public static final String CLIENT_ID = "id";
    ArrayList<String> message = new ArrayList<>();
    private Json json;

    @Before
    public void setUp() {
        clear();
    }

    @Test
    public void testSubscriptionAndUnsubscriptionUsingURI() throws RegistryException {
        clear();
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        final WampClient client = controller.getClientById(CLIENT_ID).getValue();
        assertThat(client.isSubscribed("http://example.com:9001/wamp/topic")).isFalse();

        clear();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        assertThat(client.isSubscribed("http://example.com:9001/wamp/topic")).isTrue();

        msg = json.newArray();
        msg.add(MessageType.UNSUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        assertThat(client.isSubscribed("http://example.com:9001/wamp/topic")).isFalse();
    }

    @Test
    public void testSubscriptionAndUnsubscriptionUsingCURI() throws RegistryException {
        clear();
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        final WampClient client = controller.getClientById(CLIENT_ID).getValue();
        assertThat(client.isSubscribed("http://example.com:9001/wamp/topic")).isFalse();

        client.registerPrefix("event", "http://example.com:9001/wamp/");

        clear();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("event:topic");
        controller.onMessage(CLIENT_ID, msg);

        assertThat(client.isSubscribed("http://example.com:9001/wamp/topic")).isTrue();

        msg = json.newArray();
        msg.add(MessageType.UNSUBSCRIBE.code());
        msg.add("event:topic");
        controller.onMessage(CLIENT_ID, msg);

        assertThat(client.isSubscribed("http://example.com:9001/wamp/topic")).isFalse();
    }

    @Test
    public void testSubscriptionAndUnsubscriptionOfUnknownClients() throws RegistryException {
        clear();
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        final WampClient client = controller.getClientById(CLIENT_ID).getValue();
        assertThat(client.isSubscribed("http://example.com:9001/wamp/topic")).isFalse();

        clear();
        assertThat(last()).isNull();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage("unknown", msg);

        assertThat(last()).isNull();

        msg = json.newArray();
        msg.add(MessageType.UNSUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage("unknown", msg);

        assertThat(last()).isNull();
    }

    /**
     * Use the following PUBLISH message format: [TYPE, URI, Event]
     * @throws RegistryException
     */
    @Test
    public void testPublicationAndReception() throws RegistryException {
        clear();
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        controller.open("id2");
        clear();

        // client 1 subscribes to a topic
        clear();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        // client 2 send an event on the topic, client 2 is not a subscriber of the topic

        // Simple payload
        clear();
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        controller.onMessage("id2", msg);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(last());
        assertThat(node.isArray());
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");

        // Null payload
        clear();
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add(NullNode.instance);
        controller.onMessage("id2", msg);

        assertThat(message).isNotNull();
        node = json.parse(last());
        assertThat(node.isArray());
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).isNull()).isTrue();

        // Complex payload
        clear();
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        Struct reference = new Struct("hi", 1000, true, new String[] {"a", "b"});
        msg.add(json.toJson(reference));
        controller.onMessage("id2", msg);

        assertThat(message).isNotNull();
        node = json.parse(last());
        assertThat(node.isArray());
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(json.fromJson(node.get(2), Struct.class)).isEqualTo(reference);
    }

    /**
     * Use the following PUBLISH message format: [TYPE, URI, Event, ExcludeMe]
     * @throws RegistryException
     */
    @Test
    public void testExcludeMe() throws RegistryException {
        clear();
        json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        controller.open("id2");
        clear();

        // client 1 subscribes to a topic
        clear();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        // client 2 subscribes to the same topic
        clear();
        msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage("id2", msg);

        // client 2 sends an event on the topic, client 2 is a subscriber of the topic
        clear();
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        controller.onMessage("id2", msg);

        // We have two messages in the queue
        assertThat(message.size()).isEqualTo(2);
        ArrayNode node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
        node = get(1);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");

        clear();
        // Send the same message but with the ignoreMe option set to true
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add(true);
        controller.onMessage("id2", msg);

        // We have only one message in the queue
        assertThat(message.size()).isEqualTo(1);
        node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");

        clear();

        // Send the same message but with the ignoreMe option set to false
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add(false);
        controller.onMessage("id2", msg);

        assertThat(message.size()).isEqualTo(2);
        node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
        node = get(1);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
    }

    /**
     * Use the following PUBLISH message format: [TYPE, URI, Event, Exclusions]
     * @throws RegistryException
     */
    @Test
    public void testExclusion() throws RegistryException {
        clear();
        json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        controller.open("id2");
        clear();

        WampClient client2 = controller.getClientById("id2").getValue();

        // client 1 subscribes to a topic
        clear();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        // client 2 subscribes to the same topic
        clear();
        msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage("id2", msg);

        // client 2 sends an event on the topic, client 2 is a subscriber of the topic
        clear();
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        controller.onMessage("id2", msg);

        // We have two messages in the queue
        assertThat(message.size()).isEqualTo(2);
        ArrayNode node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
        node = get(1);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");

        clear();
        // Send the same message but with the an exclude list with client2 (the sender)
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add(json.newArray().add(client2.session()));
        controller.onMessage("id2", msg);

        // We have only one message in the queue
        assertThat(message.size()).isEqualTo(1);
        node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");

        clear();

        // Send the same message but with an empty exclude list
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add(json.newArray());
        controller.onMessage("id2", msg);

        assertThat(message.size()).isEqualTo(2);
        node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
        node = get(1);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
    }

    /**
     * Use the following PUBLISH message format: [TYPE, URI, Event, Exclusions, Eligible]
     * @throws RegistryException
     */
    @Test
    public void testEligible() throws RegistryException {
        clear();
        json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        controller.open("id2");
        clear();

        WampClient client1 = controller.getClientById(CLIENT_ID).getValue();
        WampClient client2 = controller.getClientById("id2").getValue();

        // client 1 subscribes to a topic
        clear();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        // client 2 subscribes to the same topic
        clear();
        msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage("id2", msg);

        // client 2 sends an event on the topic, client 2 is a subscriber of the topic
        clear();
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        controller.onMessage("id2", msg);

        // We have two messages in the queue
        assertThat(message.size()).isEqualTo(2);
        ArrayNode node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
        node = get(1);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");

        clear();
        // Send the same message but with the an empty exclude list, but only the client 1 in the eligible list
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add(json.newArray());
        msg.add(json.newArray().add(client1.session()));
        controller.onMessage("id2", msg);

        // We have only one message in the queue
        assertThat(message.size()).isEqualTo(1);
        node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");

        clear();

        // Send the same message but with an empty exclude list, and empty eligible list
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add(json.newArray());
        msg.add(json.newArray());
        controller.onMessage("id2", msg);

        assertThat(message.size()).isEqualTo(0);

        clear();

        // Send the same message but with an empty exclude list, and eligible list containing the 2 clients
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add(json.newArray());
        msg.add(json.newArray().add(client1.session()).add(client2.session()));
        controller.onMessage("id2", msg);

        assertThat(message.size()).isEqualTo(2);
        node = get(0);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
        node = get(1);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.EVENT.code());
        assertThat(node.get(1).asText()).isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(node.get(2).asText()).isEqualTo("hello");
    }

    @Test
    public void testInvalidPublishMessage() throws RegistryException {
        clear();
        json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        // client 1 subscribes to a topic
        clear();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        // Try to send a message from unknown client
        clear();
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        controller.onMessage("unknown", msg);

        assertThat(message).isEmpty();

        // Try to send a message without topic
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        controller.onMessage("unknown", msg);

        assertThat(message).isEmpty();

        // Try to send a message with no payload
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        controller.onMessage(CLIENT_ID, msg);

        assertThat(message).isEmpty();

        // Try to send a message with an inconsistent third argument
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add("inconsistent");
        controller.onMessage(CLIENT_ID, msg);

        assertThat(message).isEmpty();

        // Try to send a message with inconsistent third and fourth arguments
        msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        msg.add("inconsistent");
        msg.add("inconsistent-2");
        controller.onMessage(CLIENT_ID, msg);

        assertThat(message).isEmpty();

    }

    private void clear() {
        message.clear();
    }

    private String last() {
        if (message.isEmpty()) {
            return null;
        }
        return message.get(0);
    }

    private ArrayNode get(int index) {
       return (ArrayNode) json.parse(message.get(index));
    }

    private WampController createWampControllerAndConnectClient(Json json) {
        Publisher publisher = mock(Publisher.class);
        final Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                message.add(0, (String) invocation.getArguments()[2]);
                return null;
            }
        };
        doAnswer(answer).when(publisher).send(anyString(), anyString(), anyString());

        WampController controller = new WampController(json, publisher, TestConstants.PREFIX);
        controller.ea = mock(EventAdmin.class);
        controller.open(CLIENT_ID);
        assertThat(message).isNotNull();
        return controller;
    }


}
