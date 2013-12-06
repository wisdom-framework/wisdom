package org.wisdom.template.thymeleaf.impl;

import com.google.common.collect.ImmutableList;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.util.List;

/**
 *
 */
public class FakeController implements Controller {


    public Result retrieve() {
        return Results.ok("hello");
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.<Route>of(
                new RouteBuilder().route(HttpMethod.GET).on("/").to(this, "retrieve")
        );
    }
}
