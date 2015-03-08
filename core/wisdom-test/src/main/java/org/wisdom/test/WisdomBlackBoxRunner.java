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

import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.test.internals.ChameleonExecutor;
import org.wisdom.test.internals.ProbeBundleMaker;
import org.wisdom.test.internals.RunnerUtils;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.io.File;
import java.lang.reflect.Method;

/**
 * The Wisdom Test Runner that executes test from outside the Wisdom runtime.
 */
public class WisdomBlackBoxRunner extends BlockJUnit4ClassRunner implements Filterable, Sortable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomBlackBoxRunner.class);

    /**
     * Creates an instance of runner.
     *
     * @param klass the class to run (test case)
     * @throws Exception if the instance of runner cannot be done.
     */
    public WisdomBlackBoxRunner(Class<?> klass) throws Exception {
        super(klass);
        File basedir = RunnerUtils.checkWisdomInstallation();
        File bundle = RunnerUtils.detectApplicationBundleIfExist(new File(basedir, "application"));
        if (bundle != null && bundle.exists()) {
            LOGGER.info("Application bundle found in the application directory (" + bundle.getAbsoluteFile() + "), " +
                    "the bundle will be deleted and replaced by the tested bundle (with the very same content).");
            LOGGER.debug("Deleting ? : " + bundle.delete());
        }
        bundle = RunnerUtils.detectApplicationBundleIfExist(new File(basedir, "runtime"));
        if (bundle != null && bundle.exists()) {
            LOGGER.info("Application bundle found in the runtime directory (" + bundle.getAbsoluteFile() + "), " +
                    "the bundle will be deleted and replaced by the tested bundle (with the very same content).");
            LOGGER.debug("Deleting ? : " + bundle.delete());
        }

        System.setProperty("application.configuration",
                new File(basedir, "/conf/application.conf").getAbsolutePath());

        ChameleonExecutor.instance(basedir);
        ChameleonExecutor.deployApplication();
    }

    @Override
    public void sort(Sorter sorter) {
        super.sort(sorter);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        super.filter(filter);
    }
}
