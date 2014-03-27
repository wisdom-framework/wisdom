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

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.wamp.messages.MessageType;
import org.wisdom.wamp.utils.JsonService;
import org.wisdom.wamp.utils.TestConstants;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Created by clement on 27/01/2014.
 */
public class WampControllerPrefixTest {

    String message;

    @Test
    public void testPrefix() {
        message = null;
        Json json = new JsonService();
        Publisher publisher = mock(Publisher.class);
        final Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                message = (String) invocation.getArguments()[2];
                return null;
            }
        };
        doAnswer(answer).when(publisher).send(anyString(), anyString(), anyString());

        WampController controller = new WampController(json, publisher, TestConstants.PREFIX);
        controller.open("id");
        assertThat(message).isNotNull();

        ArrayNode prefix = json.newArray();
        prefix.add(MessageType.PREFIX.code());
        prefix.add("calc");
        prefix.add("http://example.com/simple/calc#");
        controller.onMessage("id", prefix);

        Map.Entry<String, WampClient> entry = controller.getClientById("id");
        assertThat(entry).isNotNull();
        assertThat(entry.getValue().getUri("calc:square")).isEqualTo("http://example.com/simple/calc#square");
        assertThat(entry.getValue().getUri("http://perdu.com")).isEqualTo("http://perdu.com");
    }

    @Test
    public void testPrefixFromUnknownClient() {
        message = null;
        Json json = new JsonService();
        Publisher publisher = mock(Publisher.class);
        final Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                message = (String) invocation.getArguments()[2];
                return null;
            }
        };
        doAnswer(answer).when(publisher).send(anyString(), anyString(), anyString());

        WampController controller = new WampController(json, publisher, TestConstants.PREFIX);
        controller.open("id");
        assertThat(message).isNotNull();

        ArrayNode prefix = json.newArray();
        prefix.add(MessageType.PREFIX.code());
        prefix.add("calc");
        prefix.add("http://example.com/simple/calc#");
        controller.onMessage("unknown", prefix);

        Map.Entry<String, WampClient> entry = controller.getClientById("id");
        assertThat(entry).isNotNull();
        assertThat(entry.getValue().getUri("calc:square")).isNotEqualTo("http://example.com/simple/calc#square");
        assertThat(entry.getValue().getUri("http://perdu.com")).isEqualTo("http://perdu.com");
    }

    @Test
    public void testMissingPrefix() {
        message = null;
        Json json = new JsonService();
        Publisher publisher = mock(Publisher.class);
        final Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                message = (String) invocation.getArguments()[2];
                return null;
            }
        };
        doAnswer(answer).when(publisher).send(anyString(), anyString(), anyString());

        WampController controller = new WampController(json, publisher, TestConstants.PREFIX);
        controller.open("id");
        assertThat(message).isNotNull();
        Map.Entry<String, WampClient> entry = controller.getClientById("id");
        assertThat(entry).isNotNull();
        assertThat(entry.getValue().getUri("calc:square")).isEqualTo("calc:square");
        assertThat(entry.getValue().getUri("http://perdu.com")).isEqualTo("http://perdu.com");
    }
}
