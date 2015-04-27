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

import org.junit.runner.Description;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.test.internals.ChameleonExecutor;
import org.wisdom.test.internals.RunnerUtils;
import org.wisdom.test.shared.InVivoRunner;

import java.io.File;

/**
 * The Wisdom Test Runner.
 */
public class WisdomRunner extends BlockJUnit4ClassRunner implements Filterable, Sortable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomRunner.class);
    private final InVivoRunner delegate;

    public WisdomRunner(Class<?> klass) throws Exception {
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
        ChameleonExecutor executor = ChameleonExecutor.instance(basedir);

        executor.deployApplication();
        executor.deployProbe();

        delegate = executor.getInVivoRunnerInstance(klass);
    }


    @Override
    protected Object createTest() throws Exception {
        return delegate.createTest();
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        delegate.filter(filter);
    }

    @Override
    public void sort(Sorter sorter) {
        delegate.sort(sorter);
    }
}
