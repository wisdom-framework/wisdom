
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