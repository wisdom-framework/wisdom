package org.wisdom.engine.server;

import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.error.ErrorHandler;
import org.wisdom.api.router.Router;
import org.wisdom.akka.AkkaSystemService;

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
    public final Dispatcher dispatcher;

    public ServiceAccessor(Crypto crypto, ApplicationConfiguration configuration, Router router,
                           ContentEngine engine, AkkaSystemService system, List<ErrorHandler> handlers, Dispatcher dispatcher) {
        this.crypto = crypto;
        this.configuration = configuration;
        this.router = router;
        this.content_engines = engine;
        this.system = system;
        this.handlers = handlers;
        this.dispatcher = dispatcher;
    }


}
