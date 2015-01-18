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
package org.wisdom.executors.context;

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
