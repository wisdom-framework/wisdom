/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2017 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.browserwatch;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Closed;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.OnMessage;
import org.wisdom.api.annotations.Opened;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Controller that will notify websocket listeners that main application bundle
 * got reloaded
 * 
 * @author ndelsaux
 *
 */
@Controller
public class BrowserWatchController extends DefaultController  implements BundleTrackerCustomizer<BundleInfos>, ServiceListener {
	private static final String BROWSER_WATCH_BUNDLES = "/browserWatch/bundles";

	private static final String BROWSER_WATCH_SOCKET = "/browserWatch/socket";

	@Requires LogService log;

	/**
	 * Router is used to associate currently displayed pages (as obtained by {@link #addedClient(String, String)})
	 * with bundles and their lifecycle.
	 */
    @Requires Router router;

    /**
     * This web-socket publisher allows browsers to receive various ntofications (most important being
     * the ones regarding services reload)
     */
	@Requires private Publisher publisher;

	@Context BundleContext context;
	/**
	 * Contains the collection of controllers clients are currently viewing, organized as a 
	 * map to make sure we correctly follow clients
	 */
	private BiMap<String, String> displayedControllers = HashBiMap.create();

	@View("bundles") Template template; 
	
	private BundleTracker<BundleInfos> tracker;
	
	private BiMap<Bundle, BundleInfos> bundleInfosMapping = HashBiMap.create();
	
	/**
	 * Simple test route showing all loaded bundles, and the route of the current one
	 * @return
	 */
	@Route(method = HttpMethod.GET, uri = BROWSER_WATCH_BUNDLES)
	public Result bundles() {
		return ok(render(template));
	}

	/**
	 * When a client is added, we get the service it should be associated with using the route resoluton
	 * mechanism wisdom provides us
	 * @param client
	 * @param url
	 */
	@Opened(BROWSER_WATCH_SOCKET)
	public void addedClient(@Parameter("client") String client) {
		log.log(LogService.LOG_INFO, String.format("added client %s", client));
	}

	private org.wisdom.api.router.Route getRoute(String url) {
		for(HttpMethod m : HttpMethod.values()) {
			org.wisdom.api.router.Route r = router.getRouteFor(m, url);
			if(!r.isUnbound()) {
				log.log(LogService.LOG_INFO, String.format("Url %s is mapped to route %s", url, r));
				return r;
			}
		}
		return null;
	}

	/**
	 * And when client leaves, stop notifying it
	 * 
	 * @param client
	 *            client to remove
	 */
	@Closed(BROWSER_WATCH_SOCKET)
	public void removedClient(@Parameter("client") String client) {
		displayedControllers.inverse().remove(client);
	}
	
	@OnMessage(BROWSER_WATCH_SOCKET)
	public void receiveClientIp(@Parameter("client") String client, @Body String url) {
		log.log(LogService.LOG_INFO, String.format("client %s is browsing URL %s", client, url));
		org.wisdom.api.router.Route visitedRoute = getRoute(url);
		if(visitedRoute!=null) {
			log.log(LogService.LOG_INFO, String.format("The url %s is mapped on route %s", url, visitedRoute));
			displayedControllers.put(visitedRoute.getControllerClass().getName(), client);
		}
	}
	
	@Validate
	public void listenServices() {
		tracker = new BundleTracker<BundleInfos>(context, Bundle.ACTIVE, this);
		tracker.open();
		context.addServiceListener(this);
	}
	
	@Invalidate
	public void stopListening() {
		tracker.close();
		context.removeServiceListener(this);
	}
	
	public BundleInfos addingBundle(Bundle bundle, BundleEvent event) {
		log.log(LogService.LOG_INFO, String.format("adding bundle %s", bundle));
		return lazyGetInfos(bundle);
	}

	private BundleInfos lazyGetInfos(Bundle bundle) {
		if(!bundleInfosMapping.containsKey(bundle)) {
			BundleInfos returned = new BundleInfos();
			bundleInfosMapping.put(bundle, returned);
		}
		return bundleInfosMapping.get(bundle);
	}


	public void modifiedBundle(Bundle bundle, BundleEvent event, BundleInfos infos) {
		log.log(LogService.LOG_INFO, String.format("modifying bundle %s", bundle));
		triggerReloadFor(infos);
	}


	public void triggerReloadFor(BundleInfos infos) {
		for(String controller : infos.getControllerClassNames()) {
			triggerReloadFor(controller);
		}
	}

	public void triggerReloadFor(String controllerClass) {
		if(displayedControllers.containsKey(controllerClass)) {
			log.log(LogService.LOG_INFO, String.format("Controller %s triggered client-side reload !", controllerClass));
			publisher.send(BROWSER_WATCH_SOCKET, displayedControllers.get(controllerClass), "reload");
		}
	}

	public void removedBundle(Bundle bundle, BundleEvent event, BundleInfos infos) {
		log.log(LogService.LOG_DEBUG, String.format("removing bundle %s", bundle));
		// Just forget all infos, the list of displayed controllers is enough
		bundleInfosMapping.remove(bundle);
	}

	public void serviceChanged(ServiceEvent event) {
		Bundle bundle = event.getServiceReference().getBundle();
		switch(event.getType()) {
		case ServiceEvent.REGISTERED:
			Object service = context.getService(event.getServiceReference());
			if(service instanceof  org.wisdom.api.Controller) {
				String serviceClassName = service.getClass().getName();
				triggerReloadFor(serviceClassName);
				lazyGetInfos(bundle).addController(serviceClassName);
				log.log(LogService.LOG_INFO, String.format("Controller %s is registered !", serviceClassName));
			}
		}
	}
}
