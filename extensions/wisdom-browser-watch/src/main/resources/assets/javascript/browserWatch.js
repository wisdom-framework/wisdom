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

function openWebSocket() {
	var browserWatchWS = "ws://"+window.location.host+"/browserWatch/socket"
	var ws = new WebSocket(browserWatchWS)
	ws.onopen = function() {
		console.log("browserwatch: socket opened, sending welcome message")
		ws.send(window.location.pathname)
	}
	ws.onmessage = function(event) {
		console.log("browserwatch: received message from server : "+event)
		window.location.reload()
	}
	ws.onerror = function(event) {
		// Reload using XMLHttpRequest
		function reloadUsingXMLHttpRequest() {
			// Notice we do not expect any error code, as page should continue to load
			var xhr = new XMLHttpRequest();
			console.log('UNSENT', xhr.status);
	
			xhr.open('GET', window.location.pathname, true);
			console.log('Getting '+window.location.pathname, xhr.status);
	
			xhr.onprogress = function () {
				console.log('LOADING', xhr.status);
			};
	
			xhr.onload = function () {
				console.log('Tried loading page', xhr.status);
				if(xhr.status<400) {
					console.log("page status is correct. Reloading page fully")
					// the true clear the cache for the page items
					window.location.reload(true)
				} else {
					console.log("page status is "+xhr.status+". We will retry in 1s.")
					setTimeout(reloadUsingXMLHttpRequest, 2000)
				}
			}
			xhr.send(null);
		}

		reloadUsingXMLHttpRequest()
	}
	ws.onclose = function(event) {
		console.log("browserwatch: websocket has disconnected. Nothing to do (reload should be triggered by errors)")
	}
}
openWebSocket();