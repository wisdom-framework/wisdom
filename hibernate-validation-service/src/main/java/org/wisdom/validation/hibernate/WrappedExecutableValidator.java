package org.wisdom.validation.hibernate;

import javax.validation.ConstraintViolation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * An Executable Validator delegating to the Hibernate validator, but wrapping methods triggering classloading to set
 * the TCCL
 */
public class WrappedExecutableValidator implements ExecutableValidator {

    private final ExecutableValidator delegate;

    WrappedExecutableValidator(ExecutableValidator delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateParameters(T t, Method method, Object[] objects, Class<?>... classes) {
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return delegate.validateParameters(t, method, objects, classes);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateReturnValue(T t, Method method, Object o, Class<?>... classes) {
        return delegate.validateReturnValue(t, method, o, classes);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorParameters(Constructor<? extends T> constructor, Object[] objects, Class<?>... classes) {
        return delegate.validateConstructorParameters(constructor, objects, classes);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(Constructor<? extends T> constructor, T t, Class<?>... classes) {
        return delegate.validateConstructorReturnValue(constructor, t, classes);
    }
}
