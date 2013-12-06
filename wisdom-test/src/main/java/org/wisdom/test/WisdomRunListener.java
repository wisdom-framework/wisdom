package org.wisdom.test;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.wisdom.test.internals.ChameleonExecutor;

/**
 * a run listener to stop chameleon after tests.
 */
public class WisdomRunListener extends RunListener {

    @Override
    public void testRunFinished(Result result) throws Exception {
        ChameleonExecutor.stopRunningInstance();
    }
}
