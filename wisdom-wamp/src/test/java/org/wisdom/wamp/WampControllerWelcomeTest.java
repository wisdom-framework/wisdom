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
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.wamp.messages.MessageType;
import org.wisdom.wamp.utils.JsonService;
import org.wisdom.wamp.utils.TestConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Created by clement on 27/01/2014.
 */
public class WampControllerWelcomeTest {

    String message;

    @Test
    public void testThatWelcomeIsSent() {
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
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.WELCOME.code());
        assertThat(node.get(1).asText()).isNotNull();
        assertThat(node.get(2).asInt()).isEqualTo(Constants.WAMP_PROTOCOL_VERSION);
        assertThat(node.get(3).asText()).isEqualTo(Constants.WAMP_SERVER_VERSION);
    }

    @Test
    public void testThatWelcomeIsNotResent() {
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

        message = null;

        controller.open("id");
        assertThat(message).isNull();
    }
}
