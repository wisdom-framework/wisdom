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

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.http.Status;
import org.wisdom.executors.ManagedExecutorServiceImpl;
import org.wisdom.test.parents.FakeConfiguration;
import org.wisdom.test.parents.FakeContext;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpExecutionContextServiceTest {


    private ManagedExecutorService service;

    @Before
    public void setUp() {
        Context.CONTEXT.remove();
        service = new ManagedExecutorServiceImpl("test",
                new FakeConfiguration(Collections.<String, Object>emptyMap()),
                ImmutableList.<ExecutionContextService>of(new HttpExecutionContextService()));
    }

    @After
    public void tearDown() throws InterruptedException {
        service.shutdown();
        service.awaitTermination(100, TimeUnit.MICROSECONDS);
        Context.CONTEXT.remove();
    }

    @Test
    public void testThatTheHttpContextIfCorrectlyMigrated()
            throws ExecutionException, InterruptedException {
        Callable<Result> computation = new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                Context context = Context.CONTEXT.get();
                if (context != null && context.parameter("foo").equals("bar")) {
                    return Results.ok();
                } else {
                    return Results.badRequest();
                }
            }
        };

        FakeContext context = new FakeContext().setParameter("foo", "bar");
        Context.CONTEXT.set(context);
        Future<Result> future = service.submit(computation);

        assertThat(future.get().getStatusCode()).isEqualTo(Status.OK);
    }

}