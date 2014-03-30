package org.wisdom.api.http;

import org.junit.Test;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the syntax to build an async result
 */
public class AsyncResultTest {

    @Test
    public void testAsync() throws Exception {
        AsyncResult async = new AsyncResult(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return Results.ok();
            }
        });

        assertThat(async.callable()).isNotNull();
        assertThat(async.callable().call().getStatusCode()).isEqualTo(200);
    }
}
