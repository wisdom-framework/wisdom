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
