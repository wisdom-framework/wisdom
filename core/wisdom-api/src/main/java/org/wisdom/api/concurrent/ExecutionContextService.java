package org.wisdom.api.concurrent;

/**
 * A service exposed by entities willing to configure the execution environment
 * of a thread (such as {@link java.lang.ThreadLocal}.
 * <p>
 * An instance of {@link org.wisdom.api.concurrent.ExecutionContext} is created
 * when the task is submitted (in the caller thread) and applied before the
 * execution of the task (in the task's execution thread). The context in
 * un-applied when the task completes (also called in the task's execution
 * thread).
 */
public interface ExecutionContextService {

    /**
     * The name of the execution context service
     *
     * @return the name, such as "http context" or "transaction"
     */
    public String name();

    /**
     * Creates an {link ExecutionContext} that is applied before the task
     * execution. It should store the data to set up in the task's thread.
     *
     * @return the execution context, {@code null} if the caller thread does not
     * have meaningful data to be stored and restored.
     */
    public ExecutionContext prepare();

}
