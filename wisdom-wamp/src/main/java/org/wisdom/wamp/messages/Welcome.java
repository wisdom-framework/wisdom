package org.wisdom.wamp.messages;

import org.wisdom.wamp.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * The WAMP Welcome message.
 * http://wamp.ws/spec/#welcome_message
 */
public class Welcome extends Message {
    final String clientID;

    public Welcome(String session) {
        this.clientID = session;
    }

    @Override
    public MessageType getType() {
        return MessageType.WELCOME;
    }

    @Override
    public List<Object> toList() {
        List<Object> res = new ArrayList<>();
        res.add(getType().code());
        res.add(this.clientID);
        res.add(Constants.WAMP_PROTOCOL_VERSION);
        res.add(Constants.WAMP_SERVER_VERSION);
        return res;
    }

}
