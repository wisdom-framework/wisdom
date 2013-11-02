package org.ow2.chameleon.wisdom.engine.server;

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.wisdom.akka.AkkaSystemService;
import org.ow2.chameleon.wisdom.api.bodyparser.BodyParserEngine;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.crypto.Crypto;
import org.ow2.chameleon.wisdom.api.error.ErrorHandler;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The wisdom main servlet.
 */
@Component
@Instantiate
public class Dispatcher {

    public static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);
    private final ServiceAccessor accessor;

    @Requires
    private Router router;
    @Requires
    private ApplicationConfiguration configuration;
    @Requires
    private BodyParserEngine parsers;
    @Requires
    private Crypto crypto;
    @Requires
    private AkkaSystemService system;

    @Requires(specification = ErrorHandler.class, optional = true)
    private List<ErrorHandler> handlers;

    private final WisdomServer wisdomServer;

    public Dispatcher() throws InterruptedException {
        accessor = new ServiceAccessor(crypto, configuration, router, parsers, system, handlers);
        wisdomServer = new WisdomServer(accessor);
    }

    @Validate
    public void start() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    wisdomServer.start();
                } catch (InterruptedException e) {
                    LOGGER.error("Cannot start the Wisdom server", e);
                }
            }
        }).start();
    }

    @Invalidate
    public void stop() {
        wisdomServer.stop();
    }

}
