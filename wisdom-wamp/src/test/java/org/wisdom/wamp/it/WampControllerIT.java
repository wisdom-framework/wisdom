package org.wisdom.wamp.it;

import org.junit.Test;
import org.wisdom.test.parents.ControllerTest;
import org.wisdom.wamp.WampController;
import org.wisdom.wamp.services.Wamp;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests checking the WAMP support behavior.
 */
public class WampControllerIT extends ControllerTest {

    @Inject
    WampController controller;

    @Inject
    Wamp wamp;

    @Test
    public void testExposition() throws Exception {
        assertThat(wamp).isNotNull();
        assertThat(wamp.getWampBaseUrl()).contains("http://localhost:", "/wamp");
    }
}
