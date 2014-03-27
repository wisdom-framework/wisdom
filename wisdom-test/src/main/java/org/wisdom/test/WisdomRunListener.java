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
package org.wisdom.test;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.wisdom.test.internals.ChameleonExecutor;

/**
 * A run listener to stop chameleon after tests.
 */
public class WisdomRunListener extends RunListener {

    /**
     * Notifies the ChameleonExecutor to stop the running Chameleon instance.
     * @param result the test result (ignored)
     * @throws Exception if something bad happened
     */
    @Override
    public void testRunFinished(Result result) throws Exception {
        ChameleonExecutor.stopRunningInstance();
    }
}
