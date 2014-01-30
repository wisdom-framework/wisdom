package org.wisdom.wamp.messages;

import org.junit.Test;
import org.wisdom.wamp.Constants;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check Welcome message
 */
public class WelcomeTest {
    @Test
    public void testType() throws Exception {
        Welcome welcome = new Welcome("session");
        assertThat(welcome.getType()).isEqualTo(MessageType.WELCOME);
    }

    @Test
    public void testList() throws Exception {
        Welcome welcome = new Welcome("session");
        assertThat(welcome.toList()).containsExactly(MessageType.WELCOME.code(), "session",
                Constants.WAMP_PROTOCOL_VERSION, Constants.WAMP_SERVER_VERSION);
    }
}
