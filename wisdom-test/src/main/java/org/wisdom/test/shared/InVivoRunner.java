package org.wisdom.test.shared;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.ow2.chameleon.testing.helpers.Stability;
import org.ow2.chameleon.testing.helpers.TimeUtils;
import org.wisdom.test.parents.DependencyInjector;

/**
 * A Junit Runner executed within the OSGi Framework (Wisdom)
 */
public class InVivoRunner extends BlockJUnit4ClassRunner {


    private final BundleContext context;
    private final OSGiHelper helper;

    public InVivoRunner(BundleContext context, Class<?> klass) throws InitializationError {
        super(klass);
        // Set time factor.
        TimeUtils.TIME_FACTOR = Integer.getInteger("TIME_FACTOR", 1);
        this.context = context;
        this.helper = new OSGiHelper(context);
    }

    public Object createTest() throws Exception {
        Stability.waitForStability(context);
        Object object = super.createTest();
        DependencyInjector.inject(object, context, helper);
        return object;
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        helper.dispose();
    }
}
