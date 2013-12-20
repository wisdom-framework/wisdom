package org.wisdom.test.http;

public interface Callback<T> {

    void completed(HttpResponse<T> response);

    void failed(Exception e);

    void cancelled();
}
