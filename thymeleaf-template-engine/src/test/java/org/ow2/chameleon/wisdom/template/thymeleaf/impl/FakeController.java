package org.ow2.chameleon.wisdom.template.thymeleaf.impl;

import com.google.common.collect.ImmutableList;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.http.Results;
import org.ow2.chameleon.wisdom.api.router.Route;
import org.ow2.chameleon.wisdom.api.router.RouteBuilder;

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
