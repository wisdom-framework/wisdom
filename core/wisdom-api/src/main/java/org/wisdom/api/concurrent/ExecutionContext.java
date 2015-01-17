package org.wisdom.api.concurrent;

/**
 * {link ExecutionContext} represents the context that need to be applied before
 * the execution of a task. It's made to migrate data stored in
 * {@link java.lang.ThreadLocal} when submitting a task to an executor.
 * <p>
 * The execution context is applied before the task execution in the execution
 * thread and unapply after. Notice that instances of
 * {@link org.wisdom.api.concurrent.ExecutionContext} are made in the caller
 * thread using a {@link org.wisdom.api.concurrent.ExecutionContextService}.
 */
public interface ExecutionContext {

    /**
     * Applies the execution context. It sets up the environment.
     */
    public void apply();

    /**
     * Cleans the execution context. It removes everything that has been made in
     * the {@link #apply()} method.
     */
    public void unapply();
}
