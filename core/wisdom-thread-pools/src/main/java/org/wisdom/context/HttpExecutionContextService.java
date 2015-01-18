package org.wisdom.context;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.concurrent.ExecutionContext;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.http.Context;

/**
 * Migrates HTTP context to another thread.
 */
@Component
@Provides
@Instantiate
public class HttpExecutionContextService implements ExecutionContextService {

    public static final String HTTP_CONTEXT = "http context";

    @Override
    public String name() {
        return HTTP_CONTEXT;
    }

    @Override
    public ExecutionContext prepare() {
        return new HttpContextExecution();
    }

    private static class HttpContextExecution implements ExecutionContext {

        private final Context context;

        public HttpContextExecution() {
            this.context = Context.CONTEXT.get();
        }

        @Override
        public void apply() {
            if (context != null) {
                Context.CONTEXT.set(context);
            }
        }

        @Override
        public void unapply() {
            if (context != null) {
                Context.CONTEXT.remove();
            }
        }
    }
}
