package org.wisdom.test;

import org.junit.Test;

/**
 * Checks the behavior of the WisdomRunListener.
 */
public class WisdomRunListenerTest {

    /**
     * Checks that calling the listener does not break anything even if we don't have a running chameleon.
     * @throws Exception should not happen
     */
    @Test
    public void test() throws Exception {
        WisdomRunListener listener = new WisdomRunListener();
        listener.testRunFinished(null);
    }
}
