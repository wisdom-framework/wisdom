package org.wisdom.api.http;

import java.util.concurrent.Callable;

public class AsyncResult extends Result {

    private final Callable<Result> callable;

    public AsyncResult(Callable<Result> callable) {
        this.callable = callable;
    }

    public Callable<Result> callable() {
        return callable;
    }
}
