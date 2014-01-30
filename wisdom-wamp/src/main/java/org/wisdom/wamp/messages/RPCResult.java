package org.wisdom.wamp.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * The CALLRESULT message.
 * http://wamp.ws/spec/#callresult_message
 */
public class RPCResult extends Message {

    private final String callId;
    private final Object result;

    public RPCResult(String callId, Object result) {
        this.callId = callId;
        this.result = result;
    }


    @Override
    public MessageType getType() {
        return MessageType.CALLRESULT;
    }

    @Override
    public List<Object> toList() {
        List<Object> res = new ArrayList<>();
        res.add(getType().code());
        res.add(this.callId);
        res.add(result);
        return res;
    }
}
