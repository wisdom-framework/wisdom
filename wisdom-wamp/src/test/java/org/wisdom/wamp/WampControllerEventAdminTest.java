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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.wamp.messages.MessageType;
import org.wisdom.wamp.services.Wamp;
import org.wisdom.wamp.utils.JsonService;
import org.wisdom.wamp.utils.TestConstants;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Check the behavior of the Pub / Sub logic and its integration with the Event Admin.
 */
public class WampControllerEventAdminTest {

    public static final String CLIENT_ID = "id";
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<org.osgi.service.event.Event> events = new ArrayList<>();
    private Json json = new JsonService();

    @Before
    public void setUp() {
        clear();
    }

    @Test
    public void testTopicConversionFromWampToEventAdmin() {
        WampController controller = createWampControllerAndConnectClient();
        String topic = TestConstants.PREFIX  + "/acme";
        assertThat(controller.getEventAdminTopicFromWampTopic(topic)).isEqualTo("acme");
        topic = TestConstants.PREFIX  + "/wamp/acme";
        assertThat(controller.getEventAdminTopicFromWampTopic(topic)).isEqualTo("wamp/acme");
    }

    @Test
    public void testTopicConversionFromEventAdminToWamp() {
        WampController controller = createWampControllerAndConnectClient();
        String topic = "wamp/acme";
        assertThat(controller.getWampTopicFromEventAdminTopic(topic)).isEqualTo(TestConstants.PREFIX + "/acme");

        topic = "wamp/ex/am/ple";
        assertThat(controller.getWampTopicFromEventAdminTopic(topic)).isEqualTo(TestConstants.PREFIX + "/ex/am/ple");
    }

    @Test
    public void testEventTransferOnEventAdmin() {
        WampController controller = createWampControllerAndConnectClient();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.PUBLISH.code());
        msg.add("http://example.com:9001/wamp/topic");
        msg.add("hello");
        controller.onMessage(CLIENT_ID, msg);

        // The event should have been transferred on the event admin
        assertThat(event()).isNotNull();
        assertThat(event().getTopic()).isEqualTo("topic");
        assertThat(event().getProperty(Wamp.WAMP_TOPIC_EVENT_PROPERTY))
                .isEqualTo("http://example.com:9001/wamp/topic");
        assertThat(((JsonNode) event().getProperty(Wamp.WAMP_EVENT_PROPERTY)).asText()).isEqualTo("hello");

    }

    @Test
    public void testEventTransferOnWamp() {
        WampController controller = createWampControllerAndConnectClient();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com/topic");
        controller.onMessage(CLIENT_ID, msg);

        Map<String, String> map = ImmutableMap.of("k1", "v1", "k2", "v2");
        org.osgi.service.event.Event event = new org.osgi.service.event.Event("top/ic", map);
        controller.handleEvent(event);

        // The event should have been transferred on wamp
        System.out.println(last());
        assertThat(get(0).get(1).asText()).isEqualTo(TestConstants.PREFIX + "/top/ic");
        assertThat(get(0).get(2).get("k1").asText()).isEqualTo("v1");
        assertThat(get(0).get(2).get("k2").asText()).isEqualTo("v2");
        assertThat(get(0).get(2).get(EventConstants.EVENT_TOPIC).asText()).isEqualTo("top/ic");
    }

    @Test
    public void testEventTransferOnWampUsingExclusion() {
        WampController controller = createWampControllerAndConnectClient();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com/topic");
        controller.onMessage(CLIENT_ID, msg);
        clear();
        Map<String, Object> map = ImmutableMap.<String, Object>of("k1", "v1", "k2", "v2",
                Wamp.WAMP_EXCLUSIONS_EVENT_PROPERTY, ImmutableList.of(controller.getClientById(CLIENT_ID).getValue()
                .session()));
        org.osgi.service.event.Event event = new org.osgi.service.event.Event("top/ic", map);
        controller.handleEvent(event);

        // We have excluded the receiver.
        assertThat(last()).isNull();
    }

    @Test
    public void testEventTransferOnWampUsingEligible() {
        WampController controller = createWampControllerAndConnectClient();
        ArrayNode msg = json.newArray();
        msg.add(MessageType.SUBSCRIBE.code());
        msg.add("http://example.com/topic");
        controller.onMessage(CLIENT_ID, msg);
        clear();
        Map<String, Object> map = ImmutableMap.<String, Object>of("k1", "v1", "k2", "v2",
                Wamp.WAMP_ELIGIBLE_EVENT_PROPERTY, ImmutableList.of(controller.getClientById(CLIENT_ID).getValue()
                .session()));
        org.osgi.service.event.Event event = new org.osgi.service.event.Event("top/ic", map);
        controller.handleEvent(event);

        // The event should have been transferred on wamp
        System.out.println(last());
        assertThat(get(0).get(1).asText()).isEqualTo(TestConstants.PREFIX + "/top/ic");
        assertThat(get(0).get(2).get("k1").asText()).isEqualTo("v1");
        assertThat(get(0).get(2).get("k2").asText()).isEqualTo("v2");
        assertThat(get(0).get(2).get(EventConstants.EVENT_TOPIC).asText()).isEqualTo("top/ic");
    }

    private void clear() {
        messages.clear();
        events.clear();
    }

    private String last() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(0);
    }

    private org.osgi.service.event.Event event() {
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0);
    }

    private ArrayNode get(int index) {
       return (ArrayNode) json.parse(messages.get(index));
    }

    private WampController createWampControllerAndConnectClient() {
        Publisher publisher = mock(Publisher.class);
        final Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                messages.add(0, (String) invocation.getArguments()[2]);
                return null;
            }
        };
        doAnswer(answer).when(publisher).send(anyString(), anyString(), anyString());

        WampController controller = new WampController(json, publisher, TestConstants.PREFIX);

        controller.ea = mock(EventAdmin.class);
        final Answer<Void> postAnswer = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                events.add(0, (org.osgi.service.event.Event) invocation.getArguments()[0]);
                return null;
            }
        };
        doAnswer(postAnswer).when(controller.ea).postEvent(any(org.osgi.service.event.Event.class));

        controller.open(CLIENT_ID);
        assertThat(messages).isNotNull();
        return controller;
    }


}
