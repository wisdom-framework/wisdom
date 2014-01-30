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
public class WampControllerTest {

    String message;

    @Test
    public void testClientManagement() {
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

        assertThat(controller.getClientById("id")).isNotNull();

        controller.close("id");

        assertThat(controller.getClientById("id")).isNull();
        assertThat(controller.getClientById("id2")).isNull();

    }

    @Test
    public void testRemovingAnUnknownClient() {
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

        controller.close("unknown");
    }
}
