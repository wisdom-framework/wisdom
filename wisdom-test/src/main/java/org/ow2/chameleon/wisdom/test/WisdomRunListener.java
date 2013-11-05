package org.ow2.chameleon.wisdom.test;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.ow2.chameleon.wisdom.test.internals.ChameleonExecutor;

/**
 * a run listener to stop chameleon after tests.
 */
public class WisdomRunListener extends RunListener {

    @Override
    public void testRunFinished(Result result) throws Exception {
        ChameleonExecutor.stopRunningInstance();
    }
}
