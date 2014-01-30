package org.wisdom.akka.impl;

import org.wisdom.api.http.Context;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;

/**
 * An Akka execution context delegating to another one but setting the http context and thread context class loader.
 */
public class HttpExecutionContext implements ExecutionContextExecutor {

    private final ExecutionContext delegate;
    private final Context context;
    private final ClassLoader tccl;

    public HttpExecutionContext(ExecutionContext delegate, Context context, ClassLoader tccl) {
        this.delegate = delegate;
        this.context = context;
        this.tccl = tccl;
    }

    @Override
    public ExecutionContext prepare() {
        return delegate.prepare();
    }

    @Override
    public void execute(final Runnable runnable) {
        delegate.execute(new Runnable() {
            @Override
            public void run() {
                Thread thread = Thread.currentThread();
                ClassLoader oldContextClassLoader = thread.getContextClassLoader();
                Context oldHttpContext = Context.CONTEXT.get();
                thread.setContextClassLoader(tccl);
                Context.CONTEXT.set(context);
                try {
                    runnable.run();
                } finally {
                    thread.setContextClassLoader(oldContextClassLoader);
                    Context.CONTEXT.set(oldHttpContext);
                }
            }
        });
    }

    /**
     * Reports that an asynchronous computation failed.
     */
    @Override
    public void reportFailure(Throwable t) {
        delegate.reportFailure(t);
    }
}
