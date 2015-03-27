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
package snippets.tasks;

import com.google.common.util.concurrent.MoreExecutors;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedFutureTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * A component with scheduled tasks.
 */
// tag::scheduled[]
@Component
@Instantiate
public class ExecutorCallbackSample {

    @Requires(filter = "(name=" + ManagedExecutorService.SYSTEM + ")", proxy = false)
    ManagedExecutorService service;

    public void doSomething() {
        service.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "heavy computation has been done";
            }
        }).onSuccess(new ManagedFutureTask.SuccessCallback<String>() {
            @Override
            public void onSuccess(ManagedFutureTask<String> future, String result) {
                System.out.println("Task has returned " + result);
            }
        }).onFailure(new ManagedFutureTask.FailureCallback() {
            @Override
            public void onFailure(ManagedFutureTask future, Throwable throwable) {
                System.out.println("Task has thrown an exception");
            }
        }, MoreExecutors.directExecutor());
    }

}
// end::scheduled[]
