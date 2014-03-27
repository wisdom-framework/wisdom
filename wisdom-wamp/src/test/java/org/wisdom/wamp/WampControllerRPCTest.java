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
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.wamp.data.Calc;
import org.wisdom.wamp.data.Howdy;
import org.wisdom.wamp.data.Struct;
import org.wisdom.wamp.messages.MessageType;
import org.wisdom.wamp.services.ExportedService;
import org.wisdom.wamp.services.RegistryException;
import org.wisdom.wamp.utils.JsonService;
import org.wisdom.wamp.utils.TestConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Created by clement on 27/01/2014.
 */
public class WampControllerRPCTest {

    String message;

    //TODO properties
    //TODO Test missing or invalid code

    @Test
    public void testValidInvocationWithoutArgsAndVoidReturn() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        call.add("http://example.com/howdy#noop");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).isNull()).isTrue();
    }

    @Test
    public void testValidInvocationWithoutArgsAndStringResult() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        call.add("http://example.com/howdy#howdy");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).asText()).isEqualTo(howdy.howdy());
    }

    @Test
    public void testValidInvocationWithoutArgsAndComplexResult() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        call.add("http://example.com/howdy#struc");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        Struct reference = new Struct("hi", 1000, true, new String[] {"a", "b"});
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).toString()).isEqualTo(json.toJson(reference).toString());
    }

    @Test
    public void testValidInvocationWithArgs() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);


        Calc calc = new Calc();
        controller.register(calc, "http://example.com/calc");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        // Use CURIE
        call.add("http://example.com/calc#add");
        call.add(1);
        call.add(2);
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).asInt()).isEqualTo(3);
    }

    @Test
    public void testValidInvocationWithArgsAndUsingCURIE() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);
        WampClient client = controller.getClientById("id").getValue();
        client.registerPrefix("calc", "http://example.com/calc#");


        Calc calc = new Calc();
        controller.register(calc, "http://example.com/calc");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        // Use CURIE
        call.add("calc:add");
        call.add(1);
        call.add(2);
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).asInt()).isEqualTo(3);
    }

    @Test
    public void testValidInvocationWithComplexArg() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        Struct reference = new Struct("hi", 1000, true, new String[] {"a", "b"});
        String jr = json.toJson(reference).toString();
        call.add("http://example.com/howdy#complex");
        call.add(json.toJson(reference));

        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).toString()).isEqualTo(jr);
    }

    @Test
    public void testValidInvocationWithNullArg() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        call.add("http://example.com/howdy#complex");
        call.add(NullNode.getInstance());

        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).isNull()).isTrue();
    }

    @Test
    public void testValidInvocationWithArrayArgsUsingCURIE() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);
        WampClient client = controller.getClientById("id").getValue();
        client.registerPrefix("calc", "http://example.com/calc#");


        Calc calc = new Calc();
        controller.register(calc, "http://example.com/calc");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        // Use CURIE
        call.add("calc:sum");
        ArrayNode numbers = json.newArray();
        numbers.add(9).add(1).add(3).add(4);
        call.add(numbers);
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).asInt()).isEqualTo(17);
    }

    @Test
    public void testValidInvocationWithSimpleAndComplexArg() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);
        WampClient client = controller.getClientById("id").getValue();
        client.registerPrefix("howdy", "http://example.com/howdy");

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("7DK6TdN4wLiUJgNM");
        Struct reference = new Struct("hi", 1000, true, new String[] {"a", "b"});
        String jr = json.toJson(reference).toString();
        call.add("howdy:#operation");
        call.add("message");
        call.add(json.toJson(reference));

        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLRESULT.code());
        assertThat(node.get(1).asText()).isEqualTo("7DK6TdN4wLiUJgNM");
        assertThat(node.get(2).asBoolean()).isTrue();
    }

    @Test
    public void testMissingCallId() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        // Nothing else...
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        // The callId is set to 0, but this is out of spec
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#IllegalArgumentException");
        assertThat(node.get(3).asText()).contains("callId not defined in CALL");
    }

    @Test
    public void testMissingProcId() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        // Nothing else...
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#IllegalArgumentException");
        assertThat(node.get(3).asText()).contains("procId not defined in CALL");
    }

    @Test
    public void testMissingProcIdWithoutMethodSeparator() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        call.add("wrong_proc_id");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#IllegalArgumentException");
        assertThat(node.get(3).asText()).contains("Malformed procId");
    }

    @Test
    public void testMissingProcIdWithoutMethodSeparatorBecauseOfMissingPrefix() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        call.add("prefix:method");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#IllegalArgumentException");
        assertThat(node.get(3).asText()).contains("Malformed procId");
    }

    @Test
    public void testMissingService() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        // Try to call calc while having only howdy
        call.add("http://example.com/calc#add");
        call.add(1);
        call.add(2);
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#IllegalArgumentException");
        assertThat(node.get(3).asText()).contains("Service object", "not found");
    }

    @Test
    public void testMissingMethod() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        call.add("http://example.com/howdy#");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#IllegalArgumentException");
        assertThat(node.get(3).asText()).contains("Malformed method name");
    }

    @Test
    public void testMethodNotFoundInService() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        // These is no add method in howdy
        call.add("http://example.com/howdy#add");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#UnsupportedOperationException");
        assertThat(node.get(3).asText()).contains("Cannot find method");
    }

    @Test
    public void testArgumentMismatchWithNotEnoughArg() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Calc calc = new Calc();
        controller.register(calc, "http://example.com/calc");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        call.add("http://example.com/calc#add");
        call.add(1);
        // One argument missing.
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#UnsupportedOperationException");
        assertThat(node.get(3).asText()).contains("Argument mismatch");
    }

    @Test
    public void testArgumentMismatchWithTooMuchArgs() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Calc calc = new Calc();
        controller.register(calc, "http://example.com/calc");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        call.add("http://example.com/calc#add");
        call.add(1).add(1).add(1);
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#UnsupportedOperationException");
        assertThat(node.get(3).asText()).contains("Argument mismatch");
    }

    @Test
    public void testMethodThrowingException() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");

        message = null;
        ArrayNode call = json.newArray();
        call.add(MessageType.CALL.code());
        call.add("1111111");
        call.add("http://example.com/howdy#buggy");
        controller.onMessage("id", call);

        assertThat(message).isNotNull();
        JsonNode node = json.parse(message);
        assertThat(node.isArray());
        System.out.println(message);
        assertThat(node.get(0).asInt()).isEqualTo(MessageType.CALLERROR.code());
        assertThat(node.get(1).asText()).isEqualTo("1111111");
        assertThat(node.get(2).asText()).isEqualTo("http://example.com:9001/wamp/error#NullPointerException");
        assertThat(node.get(3).asText()).contains("I'm a bug");
    }

    @Test(expected = RegistryException.class)
    public void testRegistrationConflict() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");
        //The second registration should fail.
        controller.register(howdy, "http://example.com/howdy");
    }

    @Test
    public void testRegistrationCycles() throws RegistryException {
        message = null;
        Json json = new JsonService();
        WampController controller = createWampControllerAndConnectClient(json);

        Howdy howdy = new Howdy();
        controller.register(howdy, "http://example.com/howdy");
        assertThat(controller.getServices().size()).isEqualTo(1);
        controller.unregister("http://example.com/howdy");
        assertThat(controller.getServices().size()).isEqualTo(0);
        ExportedService svc = controller.register(howdy, "http://example.com/howdy");
        assertThat(controller.getServices().size()).isEqualTo(1);
        controller.unregister(svc);
        assertThat(controller.getServices().size()).isEqualTo(0);
    }

    private WampController createWampControllerAndConnectClient(Json json) {
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
        return controller;
    }


}
