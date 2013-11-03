package org.ow2.chameleon.wisdom.test;

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
        Object object = super.createTest();
        // Inject bundle context.
        injectBundleContextIfNeeded(object);
        return object;
    }

    private void injectBundleContextIfNeeded(Object object) {
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            if (field.getType().equals(BundleContext.class)) {
                field.setAccessible(true);
                try {
                    field.set(object, context);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(BundleContext.class)) {
                field.setAccessible(true);
                try {
                    field.set(object, context);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
