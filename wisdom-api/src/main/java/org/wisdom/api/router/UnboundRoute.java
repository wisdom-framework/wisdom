package org.wisdom.api.router;

import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a route without controller and method.
 * It generally results in a 404 response.
 */
public class UnboundRoute extends Route {

    private final List<RouteUtils.Argument> arguments;

    /**
     * Main constructor.
     *
     * @param httpMethod the method
     * @param uri        the uri
     */
    public UnboundRoute(HttpMethod httpMethod,
                        String uri) {
        super(httpMethod, uri, null, null);
        this.arguments = Collections.emptyList();
    }

    public boolean matches(HttpMethod method, String uri) {
        return false;
    }

    public boolean matches(String httpMethod, String uri) {
        return false;
    }

    public Map<String, String> getPathParametersEncoded(String uri) {
        return Collections.emptyMap();
    }

    public Result invoke() throws Throwable {
        return Results.notFound();
    }

    public List<RouteUtils.Argument> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "{"
                + getHttpMethod() + " " + getUrl() + " => "
                + "UNBOUND"
                + "}";
    }

    @Override
    public boolean isUnbound() {
        return true;
    }
}
