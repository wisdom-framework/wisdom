/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.api.concurrent;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link org.wisdom.api.concurrent.ExecutionContext} that
 * results from the composition of several individual
 * {@link org.wisdom.api.concurrent.ExecutionContext}. It's application is the
 * application of all the composed context.
 * <p>
 * The set of {@link org.wisdom.api.concurrent.ExecutionContext} composing an
 * instance of {@link org.wisdom.api.concurrent.CompositeExecutionContext}
 * cannot be changed after creation.
 */
public class CompositeExecutionContext implements ExecutionContext {

    private Collection<? extends ExecutionContext> elements = ImmutableList.of();

    private CompositeExecutionContext() {
        // Avoid direct instantiation.
    }

    /**
     * Creates an instance of {@link CompositeExecutionContext} composed by the
     * given {@link org.wisdom.api.concurrent.ExecutionContext}.
     *
     * @param contexts the elements that are going to compose the composite
     * @return the composite context
     */
    public static CompositeExecutionContext create(ExecutionContext... contexts) {
        return create(Arrays.asList(contexts));
    }

    /**
     * Creates an instance of {@link CompositeExecutionContext} composed by the
     * given {@link org.wisdom.api.concurrent.ExecutionContext}.
     *
     * @param contexts the elements that are going to compose the composite
     * @return the composite context
     */
    public static CompositeExecutionContext create(List<ExecutionContext> contexts) {
        return new CompositeExecutionContext().addAll(contexts);
    }

    /**
     * Sets the composing context.
     *
     * @param contexts the elements
     * @return the current instance
     */
    private CompositeExecutionContext addAll(List<ExecutionContext> contexts) {
        this.elements = new ImmutableList.Builder().addAll(contexts).build();
        return this;
    }

    /**
     * Applies the execution context.
     * It applies the different composing context in the order they were given.
     */
    @Override
    public void apply() {
        for (ExecutionContext context : elements) {
            context.apply();
        }
    }

    /**
     * Un-applies the execution context.
     * It un-applies the different composing context in the order they were given.
     */
    @Override
    public void unapply() {
        for (ExecutionContext context : elements) {
            context.unapply();
        }
    }
}
