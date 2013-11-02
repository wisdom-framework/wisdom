package org.ow2.chameleon.wisdom.api.error;

import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Request;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Route;

import java.util.Map;

/**
 * A service invoked when:
 * <ul>
 *     <li>No routes are matching the request</li>
 *     <li>A controller has thrown an exception (not wrapped within an {@link org.ow2.chameleon.wisdom.api.http
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
