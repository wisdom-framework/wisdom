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
package org.wisdom.validation.hibernate;

import javax.validation.ConstraintViolation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * An Executable Validator delegating to the Hibernate validator, but wrapping methods triggering classloading to set
 * the TCCL.
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
