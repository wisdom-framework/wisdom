package org.wisdom.context;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.http.Status;
import org.wisdom.pools.ManagedExecutorServiceImpl;
import org.wisdom.test.parents.FakeConfiguration;
import org.wisdom.test.parents.FakeContext;

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
        service = new ManagedExecutorServiceImpl(
                new FakeConfiguration(ImmutableMap.<String, Object>of("name", "test")));
        ((ManagedExecutorServiceImpl)service)
                .setExecutionContextService(new HttpExecutionContextService());
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