/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
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
(function() {
    var Sock = function() {
        var socket;
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        if (window.WebSocket) {
            // Compute the web socket url.
            // window.location.host includes the port
            var url =  "ws://" + window.location.host + "/ws/websocket";
            if (window.location.protocol == "https:") {
                url =  "wss://" + window.location.host + "/ws/websocket";
            }
            socket = new WebSocket(url);
            socket.onopen = onopen;
            socket.onmessage = onmessage;
            socket.onclose = onclose;
        } else {
            alert("Your browser does not support Web Socket.");
        }

        function onopen(event) {
            getTextAreaElement().value = "Web Socket opened!";
        }

        function onmessage(event) {
            appendTextArea(event.data);
        }
        function onclose(event) {
            appendTextArea("Web Socket closed");
        }

        function appendTextArea(newData) {
            var el = getTextAreaElement();
            el.value = el.value + '\n> ' + newData;
        }

        function getTextAreaElement() {
            return document.getElementById('responseText');
        }

        function send(event) {
            event.preventDefault();
            if (window.WebSocket) {
                if (socket.readyState == WebSocket.OPEN) {
                    var msg = {};
                    msg.message = event.target.message.value;
                    socket.send(JSON.stringify(msg));
                    //socket.send(event.target.message.value);
                } else {
                    alert("The socket is not open.");
                }
            }
        }
        document.forms.inputform.addEventListener('submit', send, false);
    };
    window.addEventListener('load', function() { new Sock(); }, false);
})();
