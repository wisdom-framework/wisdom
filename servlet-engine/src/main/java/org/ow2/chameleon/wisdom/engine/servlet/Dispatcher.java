package org.ow2.chameleon.wisdom.engine.servlet;

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.wisdom.api.bodyparser.BodyParserEngine;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.crypto.Crypto;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.http.Results;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.route.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The wisdom main servlet.
 */
@Component
@Provides(specifications = Servlet.class)
@Instantiate
public class Dispatcher extends HttpServlet {

    public static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

    @ServiceProperty
    private String alias;

    @Requires
    private Router router;

    @Requires
    private ApplicationConfiguration configuration;

    @Requires
    private BodyParserEngine parsers;

    @Requires
    private  Crypto crypto;

    private final ServiceAccessor accessor;

    public Dispatcher(@Property(value = "/wisdom") String alias) {
        accessor = new ServiceAccessor(crypto, configuration, router, parsers);

        LOGGER.info("Registering Wisdom's root servlet on " + alias);
        this.alias = alias;


    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("Attempt to serve " + req.getPathInfo());
        Context context = new ContextFromServlet(accessor, req, resp);
        Route route = router.getRouteFor(req.getMethod(), context.path());
        Result result;
        if (route == null) {
            result = Results.notFound();
        } else {
            context.setRoute(route);
            //TODO !!!!!
        }
    }

    private void buildContext(HttpServletRequest req, HttpServletResponse resp) {
        // TODO
    }
}
