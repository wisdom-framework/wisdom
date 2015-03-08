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
package org.wisdom.test.parents;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;

/**
 * Allow configuring an invocation of an action.
 */
public class Action {

    /**
     * The invocation.
     */
    private final Invocation invocation;

    /**
     * The fake context.
     */
    private FakeContext context;

    /**
     * Creates an invocation.
     *
     * @param invocation the invocation
     */
    public Action(Invocation invocation) {
        this.invocation = invocation;
        this.context = new FakeContext();
    }

    /**
     * Gets a new action using the given invocation.
     *
     * @param invocation the invocation
     * @return the new action
     */
    public static Action action(Invocation invocation) {
        return new Action(invocation);
    }

    /**
     * Just there for cosmetic reason.
     *
     * @return the current action
     */
    public Action with() {
        return this;
    }

    /**
     * Sets the context of the action to the given context.
     * @param context the fake context.
     * @return the current action.
     */
    public Action with(FakeContext context) {
        this.context = context;
        return this;
    }

    /**
     * Sets a parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the current action
     */
    public Action parameter(String name, String value) {
        context.setParameter(name, value);
        return this;
    }

    /**
     * Sets the body.
     *
     * @param object the body
     * @return the current action
     */
    public Action body(Object object) {
        context.setBody(object);
        return this;
    }

    /**
     * Sets a parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the current action
     */
    public Action parameter(String name, int value) {
        context.setParameter(name, Integer.toString(value));
        return this;
    }

    /**
     * Sets a parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the current action
     */
    public Action parameter(String name, boolean value) {
        context.setParameter(name, Boolean.toString(value));
        return this;
    }

    /**
     * Sets a parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the current action
     */
    public Action attribute(String name, String value) {
        context.setFormField(name, value);
        return this;
    }

    /**
     * Sets a header.
     *
     * @param name  the header name
     * @param value the header value
     * @return the current action
     */
    public Action header(String name, String value) {
        context.setHeader(name, value);
        return this;
    }

    /**
     * Invokes the configured action.
     *
     * @return the result
     */
    public ActionResult invoke() {
        // Set the fake context.
        Context.CONTEXT.set(context);
        // Create the request

        // Invoke
        try {
            return new ActionResult(
                    invocation.invoke(),
                    context);
        } catch (Throwable e) { //NOSONAR
            return new ActionResult(Results.internalServerError(e), context);
        } finally {
            Context.CONTEXT.remove();
        }
    }

    /**
     * Action's result.
     */
    public static class ActionResult {

        private final Result result;
        private final Context context;

        /**
         * Creates a new action result.
         *
         * @param result  the result
         * @param context the context
         */
        public ActionResult(Result result, Context context) {
            this.result = result;
            this.context = context;
        }

        /**
         * Gets the result.
         *
         * @return the result
         */
        public Result getResult() {
            return result;
        }

        /**
         * Gets the context.
         *
         * @return the context
         */
        public Context getContext() {
            return context;
        }
    }
}
