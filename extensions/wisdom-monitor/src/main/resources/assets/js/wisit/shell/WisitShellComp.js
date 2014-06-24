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

/* global $, Exception*/

/**
 *
 * @class WisitShellComp
 * @extends HUBU.AbstractComponent
 * @extends wisit.shell.ShellService
 */

function WisitShellComp() {
    "use strict";

    var self = this;
    var _hub;

    var _root = "/monitor/terminal";
    var _topic = "/monitor/terminal/stream";

    //Completion cache for command with second argument
    var _completionCache = {};

    //List of the shell commands.
    var _commands = null;

    self.name = "WisitShellComp";

    /**
     *
     * @method startWithInList
     * @return the elemen that match the prefix in the given list
     */
    var startWithInList = function(list, prefix) {
        var regex = new RegExp("^" + prefix, "i");
        return list.filter(function(elem) {
            return elem.match(regex);
        });
    };

    self.getComponentName = function() {
        return self.name;
    };

    /**
     * Configure the instance of the WisitShellComp component.
     *
     * @method configure
     * @param {HUBU.hub} theHub
     * @param conf - The ShellService configuration.
     * @param {root} [conf.root="/wisit"] - The url root of the wisit shell resource (WisitShellController)
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

        //register the RactiveRenderService
        _hub.provideService({
            component: self,
            contract: window.wisit.shell.ShellService,
            properties: {
                root: _root
            }
        });
    };

    self.start = function() {
        $.ajax({
            url: _root + "/command",
            type: "GET",
            async: false,
            dataType: "json"
        }).done(function(commands) {
            _commands = commands;
        }).fail(function(xhr, status, error) {
            console.warn("[" + self.name + "] cannot retrieve command: " + status + " - " + error);
        });

        //Get ipojo instances and factories autocompletion
        $.ajax({
            url: "/monitor/ipojo.json",
            type: "GET",
            async: false,
            dataType: "json"
        }).done(function(ipojo) {
            _completionCache.instance = ipojo.instances.map(function(elem){return elem.name;});
            _completionCache.factory = ipojo.factories.filter(function(elem){return !elem.handler;}).map(function(elem){return elem.name;});
        }).fail(function(xhr, status, error) {
            console.warn("[" + self.name + "] Cannot retrieve ipojo.json: " + status + " - " + error);
        });
    };

    self.stop = function() {
        _commands = null;
        _completionCache = null;
    };

    self.getCommands = function() {
        return _commands;
    };

    /**
     *
     * @method autoComplete
     * @memberof wisit.shell.ShellService
     * @param fullcommand - the shell command to be completed
     * @param callback - call with the result of completion
     */
    self.autoComplete = function(fullcommand, callback) {
        var command = fullcommand.split(" ");

        //command empty, return all shell commands
        if (command.length === 0) {
            callback(_commands);
        }

        //command does not have argument, return commands starting like it
        if (command.length === 1) {
            callback(startWithInList(_commands, command[0]));
        }

        if (command.length === 2 && command[0] in _completionCache) {
            callback(startWithInList(_completionCache[command[0]], command[1]));
        }
    };

    self.exec = function(cmd, args) {
        return $.ajax({
            url: _root + "/command/" + cmd,
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(args)
        }).done(function(data) {
            if (typeof data !== "undefined") {
                _hub.publish(self, _topic, {
                    data: data
                }); //Publish the command result if any
            }
        }).fail(function(xhr, status, error) {
            console.warn("[" + self.name + "] cannot exec command: " + status + " - " + error);
        });
    };

    self.getTopic = function() {
        return _topic;
    };
}
