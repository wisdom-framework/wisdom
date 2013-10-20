package org.ow2.chameleon.wisdom.engine.servlet;

import org.ow2.chameleon.wisdom.api.bodyparser.BodyParserEngine;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.crypto.Crypto;
import org.ow2.chameleon.wisdom.api.route.Router;

/**
 * A structure to access services.
 */
public class ServiceAccessor {

    public final Crypto crypto;
    public final ApplicationConfiguration configuration;
    public final Router router;
    public final BodyParserEngine bodyparsers;

    public ServiceAccessor(Crypto crypto, ApplicationConfiguration configuration, Router router,
                           BodyParserEngine engine) {
        this.crypto = crypto;
        this.configuration = configuration;
        this.router = router;
        this.bodyparsers = engine;
    }
}
