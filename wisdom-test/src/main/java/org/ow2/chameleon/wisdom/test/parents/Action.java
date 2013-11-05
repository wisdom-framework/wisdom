package org.ow2.chameleon.wisdom.test.parents;

import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.http.Results;

/**
 * Allow configuring an invocation of an action.
 */
public class Action {

    private final Invocation invocation;
    private final FakeContext context;

    public Action(Invocation invocation) {
        this.invocation = invocation;
        this.context = new FakeContext();
    }

    public static Action action(Invocation invocation) {
        return new Action(invocation);
    }

    public Action with() {
        return this;
    }

    public Action parameter(String name, String value) {
        context.setParameter(name, value);
        return this;
    }

    public Action parameter(String name, int value) {
        context.setParameter(name, Integer.toString(value));
        return this;
    }

    public Action parameter(String name, boolean value) {
        context.setParameter(name, Boolean.toString(value));
        return this;
    }

    public Action attribute(String name, String value) {
        context.setAttribute(name, value);
        return this;
    }

    public ActionResult invoke() {
        return _invoke();
    }

    private ActionResult _invoke() {
        // Set the fake context.
        Context.context.set(context);
        // Create the request

        // Invoke
        try {
            return new ActionResult(
                    invocation.invoke(),
                    context);
        } catch (Exception e) {
            return new ActionResult(Results.internalServerError(e), context);
        } finally {
            Context.context.remove();
        }
    }

    public static class ActionResult {

        public final Result result;
        public final Context context;

        public ActionResult(Result result, Context context) {
            this.result = result;
            this.context = context;
        }
    }
}
