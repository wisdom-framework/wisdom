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

import java.util.regex.Pattern;

import org.apache.felix.ipojo.annotations.Requires;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.osgi.service.log.LogService;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.bodies.RenderableString;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;

/**
 * When that filter is invoked, a javascript is dynamically added which will
 * open a websocket connection. This javascript will connect to
 * {@link BrowserWatchController}.
 * 
 * @author ndelsaux
 *
 */
@Service(Filter.class)
public class BrowserWatchFilter implements Filter {
	public static Pattern ALL_ROUTES = Pattern.compile("/.*");

	@Requires LogService log;

	@Requires ApplicationConfiguration configuration;

    @Requires private Router router;
    
	public Result call(Route route, RequestContext context) throws Exception {
		Result returned = context.proceed();
		// Only augment elements in DEV mode
		if (configuration.isDev()) {
			// Only "augment" HTML results
			Renderable<?> renderable = returned.getRenderable();
			if(renderable!=null) {
				if ("text/html".equals(renderable.mimetype())) {
					if (renderable instanceof RenderableString) {
						RenderableString text = (RenderableString) renderable;
						log.log(LogService.LOG_INFO, String.format("Intercepting query %s", context.context().path()));
						// Now add the magic javascript !
						returned.html().render(addJavascript(text.content()));
					}
				}
			}
		}
		return returned;
	}

	private String addJavascript(String content) {
		Document sourcePage = Jsoup.parse(content);
		sourcePage.body().lastElementSibling().after(
				String.format("<script src=\"%s\"></script>", "/assets/javascript/browserWatch.js")
				);
		return sourcePage.outerHtml();
	}

	public Pattern uri() {
		return ALL_ROUTES;
	}

	public int priority() {
		// TODO Auto-generated method stub
		return Integer.MAX_VALUE;
	}
}
