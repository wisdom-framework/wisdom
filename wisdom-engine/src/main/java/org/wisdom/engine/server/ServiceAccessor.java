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

    private final Crypto crypto;
	private final ApplicationConfiguration configuration;
    private final Router router;
    private final ContentEngine contentEngines;
    private final AkkaSystemService system;
    private final List<ErrorHandler> handlers;
    private final Dispatcher dispatcher;

    public ServiceAccessor(Crypto crypto, ApplicationConfiguration configuration, Router router,
                           ContentEngine engine, AkkaSystemService system, List<ErrorHandler> handlers, Dispatcher dispatcher) {
        this.crypto = crypto;
        this.configuration = configuration;
        this.router = router;
        this.contentEngines = engine;
        this.system = system;
        this.handlers = handlers;
        this.dispatcher = dispatcher;
    }
    
    public Crypto getCrypto() {
		return crypto;
	}

	public ApplicationConfiguration getConfiguration() {
		return configuration;
	}

	public Router getRouter() {
		return router;
	}

	public ContentEngine getContentEngines() {
		return contentEngines;
	}

	public AkkaSystemService getSystem() {
		return system;
	}

	public List<ErrorHandler> getHandlers() {
		return handlers;
	}

	public Dispatcher getDispatcher() {
		return dispatcher;
	}

}
