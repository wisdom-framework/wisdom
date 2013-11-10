package org.ow2.chameleon.wisdom.engine.server;

import org.ow2.chameleon.wisdom.api.content.ContentEngine;
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
    public final ContentEngine content_engines;
    public final AkkaSystemService system;
    public final List<ErrorHandler> handlers;

    public ServiceAccessor(Crypto crypto, ApplicationConfiguration configuration, Router router,
                           ContentEngine engine, AkkaSystemService system, List<ErrorHandler> handlers) {
        this.crypto = crypto;
        this.configuration = configuration;
        this.router = router;
        this.content_engines = engine;
        this.system = system;
        this.handlers = handlers;
    }


}
