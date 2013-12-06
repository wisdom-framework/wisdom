package org.wisdom.api.error;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;

/**
 * A service invoked when:
 * <ul>
 *     <li>No routes are matching the request</li>
 *     <li>A controller has thrown an exception (not wrapped within an {@link org.wisdom.api.http
 *     .Results.internalServerError()})</li>
 * </ul>
 *
 * Several error handlers can co-exist. There are called sequentially. The result returned by the last one is returned
 * to the client.
 *
 * TODO this model is a bit broken, especially for the delegation.
 */
public interface ErrorHandler {

    public Result onNoRoute(HttpMethod method, String uri);

    public Result onError(Context context, Route route, Throwable e);
}
