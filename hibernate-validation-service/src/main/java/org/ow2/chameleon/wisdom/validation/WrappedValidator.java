package org.ow2.chameleon.wisdom.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import java.util.Set;

/**
 * A Validator delegating to the Hibernate validator, but wrapping methods triggering classloading to set the TCCL
 */
public class WrappedValidator implements Validator {

    private final Validator delegate;

    WrappedValidator(Validator delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T t, Class<?>... classes) {
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return delegate.validate(t, classes);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(T t, String s, Class<?>... classes) {
        return delegate.validateProperty(t, s, classes);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> tClass, String s, Object o, Class<?>... classes) {
        return delegate.validateValue(tClass, s, o, classes);
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> aClass) {
        return delegate.getConstraintsForClass(aClass);
    }

    @Override
    public <T> T unwrap(Class<T> tClass) {
        return delegate.unwrap(tClass);
    }

    @Override
    public ExecutableValidator forExecutables() {
        return new WrappedExecutableValidator(delegate.forExecutables());
    }
}
