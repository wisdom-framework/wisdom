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
package org.wisdom.wamp.messages;

/**
 * The set of message type used by WAMP. Check
 * <a href="http://wamp.ws/spec/#message_types">http://wamp.ws/spec/#message_types</a> for more details.
 */
public enum MessageType {
    WELCOME(0),
    PREFIX(1),
    CALL(2),
    CALLRESULT(3),
    CALLERROR(4),
    SUBSCRIBE(5),
    UNSUBSCRIBE(6),
    PUBLISH(7),
    EVENT(8);

    private final int typeCode;

    private MessageType(int typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * @return the code of the message according to the WAMP specification.
     */
    public int code() {
        return typeCode;
    }

    /**
     * Gets the MessageType from the given code.
     * @param type the code
     * @return the MessageType
     * @throws EnumConstantNotPresentException if the code is not defined.
     */
    public static MessageType getType(int type) throws EnumConstantNotPresentException{
        for (MessageType messageType : MessageType.values()) {
            if (messageType.code() == type) {
                return messageType;
            }
        }
        throw new EnumConstantNotPresentException(MessageType.class, Integer.toString(type));
    }
}
