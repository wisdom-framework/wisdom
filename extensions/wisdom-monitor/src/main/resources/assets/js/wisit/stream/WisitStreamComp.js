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
/* global Exception*/

/**
 *
 * Implementation of the {@link window.wisit.stream.AuthService}
 *
 * @class WisitStreamComp
 * @extends HUBU.AbstractComponent
 * @extends window.wisit.stream.AuthService
 */

function WisitStreamComp() {
    "use strict";

    var self = this;
    var _hub;

    var _root = "/monitor/terminal";
    var _topic = "/monitor/terminal/stream";
    var _socket = null;

    self.name = "WisitStreamComp";

    self.getComponentName = function() {
        return self.name;
    };

    /**
     * Configure the instance of the WisitStreamComp component.
     *
     * @method configure
     * @param {HUBU.hub} theHub
     * @param conf - The StreamService configuration.
     * @param {string} [conf.root="/wisit"] - The url root of the wisit steam resource (WisitShellController)
     * @param {string} [conf.topic="/wisit/stream"] - The topic on which to map the stream
     */
    self.configure = function(theHub, conf) {
        _hub = theHub;

        if (typeof conf !== "undefined") {
            if ((typeof conf.root === "string") && conf.root.match(/^\/\w+(\/\w+)*$/)) {
                _root = conf.root;
            } else if (typeof conf.root !== "undefined") {
                throw new Exception("The property root must be a valid path string.");
            }

            if ((typeof conf.topic === "string") && conf.topic.match(/^\/\w+(\/\w+)*$/)) {
                _topic = conf.topic;
            } else if (typeof conf.topic !== "undefined") {
                throw new Exception("The property topic must be a valid topic string.");
            }
        }

        if (!window.WebSocket) {

            if (!window.MozWebSocket) {
                throw new Exception("Your browser does not support websocket!");
            }

            window.WebSocket = window.MozWebSocket;
        }

        //register the RactiveRenderService
        _hub.provideService({
            component: self,
            contract: window.wisit.stream.StreamService,
            properties: {
                root: _root
            }
        });
    };

    self.start = function() {};

    self.stop = function() {
        self.close();
    };

    self.open = function(onOpen, onClose) {
        if (_socket !== null) {
            throw new Exception("A socket has already been opened");
        }

        var url = "ws://" + window.location.host + _root + "/stream";
        if (window.location.protocol == "https:") {
            url =  "wss://" + window.location.host + _root + "/stream";
        }

        _socket = new WebSocket(url);

        _socket.onopen = onOpen;
        _socket.onmessage = function(event) {
            try {
                _hub.publish(self, _topic, {data: event.data}); //Publish the stream content (command restult)
            } catch (err) {
                console.error("["+self.name+"] "+err);
            }
        };

        _socket.onclose = onClose;
    };

    self.close = function() {
        if (_socket !== null) {
            _socket.close();
            _socket = null;
        }
    };

    self.getTopic = function() {
        return _topic;
    };
}
