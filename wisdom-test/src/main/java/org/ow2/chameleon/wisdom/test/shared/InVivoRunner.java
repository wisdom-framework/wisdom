package org.ow2.chameleon.wisdom.test.shared;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Field;

/**
 * A Junit Runner executed within the OSGi Framework (Wisdom)
 */
public class InVivoRunner extends BlockJUnit4ClassRunner {


    private final BundleContext context;

    public InVivoRunner(BundleContext context, Class<?> klass) throws InitializationError {
        super(klass);
        this.context = context;
    }

    public Object createTest() throws Exception {
        Object object = getTestClass().getJavaClass().newInstance();
        // Inject bundle context.
        injectBundleContextIfNeeded(object);
        return object;
    }

    private void injectBundleContextIfNeeded(Object object) {
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            if (field.getType().equals(BundleContext.class)  && field.getAnnotation(javax.inject.Inject.class) !=
                    null) {
                field.setAccessible(true);
                try {
                    field.set(object, context);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot inject bundle context in " + field.getName() + " from " + object
                            .getClass().getName(), e);
                }
            }
        }
        fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(BundleContext.class) && field.getAnnotation(javax.inject.Inject.class) !=
                    null) {
                field.setAccessible(true);
                try {
                    field.set(object, context);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot inject bundle context in " + field.getName() + " from " + object
                            .getClass().getName(), e);
                }
            }
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }
}
