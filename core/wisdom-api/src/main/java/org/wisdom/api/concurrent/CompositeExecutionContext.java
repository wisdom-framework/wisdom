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
