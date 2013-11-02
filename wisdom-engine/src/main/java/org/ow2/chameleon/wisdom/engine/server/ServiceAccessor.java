package org.ow2.chameleon.wisdom.engine.server;

import org.ow2.chameleon.wisdom.api.bodyparser.BodyParserEngine;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.crypto.Crypto;
import org.ow2.chameleon.wisdom.api.error.ErrorHandler;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.akka.AkkaSystemService;

import java.util.List;

/**
 * A structure to access services.
 */
public class ServiceAccessor {

    public final Crypto crypto;
    public final ApplicationConfiguration configuration;
    public final Router router;
    public final BodyParserEngine bodyparsers;
    public final AkkaSystemService system;
    public final List<ErrorHandler> handlers;

    public ServiceAccessor(Crypto crypto, ApplicationConfiguration configuration, Router router,
                           BodyParserEngine engine, AkkaSystemService system, List<ErrorHandler> handlers) {
        this.crypto = crypto;
        this.configuration = configuration;
        this.router = router;
        this.bodyparsers = engine;
        this.system = system;
        this.handlers = handlers;
    }


}
