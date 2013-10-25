package org.ow2.chameleon.wisdom.engine.server;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.bodyparser.BodyParserEngine;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.crypto.Crypto;
import org.ow2.chameleon.wisdom.api.route.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final WisdomServer wisdomServer;

    public Dispatcher() throws InterruptedException {
        accessor = new ServiceAccessor(crypto, configuration, router, parsers);

        wisdomServer = new WisdomServer(accessor);
        wisdomServer.start();
    }

    @Invalidate
    public void stop() {
        wisdomServer.stop();
    }

}
