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

/* global $, Exception, console*/

/**
 *
 * @class WisitShellComp
 * @extends HUBU.AbstractComponent
 */
function WisitTerminal() {
    "use strict";

    var self = this;
    var _hub;

    var _topic = "/monitor/terminal/stream";
    var _term;
    var _select = "#wisit";

    var _settings = {
        greetings: "                                                  \n" +
                   "      {[1;33mO[0m,[1;33mO[0m} \n" +
                   "     ./)_)     [1;36mWisdom Interactive Terminal[0m\n" +
                   "  [4m     \" \" \n\n[0m",
        width: "100%",
        height: "100%",
        checkArity: false,
        prompt: "admin@wisdom>",
        onBlur: function() {
            // the height of the body is only 2 lines initialy
            return false;
        },
        exit: false
    };

    //catch the strange [0m and [m ansi who must be replace by [0m
    var _ansireplace = new RegExp("\\\\[([0-9]{1,2};0m|m)","g");

    /**
     * Format the command result.
     * @method
     */
    function format(data) {
        var head = data.substr(0, 3);
        var ret = {};

        //format the raw data
        if (head === "res" || head === "err") {
            ret[head] = data.substr(4).replace(_ansireplace, "[0m");
        }

        //we received an empty command.
        return ret;
    }


    self.auth = null; //AuthService
    self.shell = null; //ShellService
    self.stream = null; //StreamService

    self.name = "WisitTerminal";

    function receiveResult(event) {
        var data = format(event.data);

        if (typeof data.res === "string") {
            _term.echo(data.res);
        }
        if (typeof data.err === "string") {
            _term.error(data.err);
        }
    }

    self.getComponentName = function() {
        return self.name;
    };

    /**
     * Configure the instance of the WisitTerminal component.
     *
     * @method configure
     * @param {HUBU.hub} theHub
     * @param conf - The WisitTerminal configuration.
     * @param {conf.topic} [conf.topic="/wisit/stream"] - The topic on which command result are publish!
     * @param {conf.auth} [conf.auth=true] - False in order to use the terminal without authentication.
     */
    self.configure = function(theHub, conf) {
        _hub = theHub;

        if (typeof conf !== "undefined") {
            //If the property `topic` has been define, check if valid and use it.
            if ((typeof conf.topic === "string") && conf.topic.match(/^\/\w+(\/\w+)*$/)) {
                _topic = conf.topic;
            } else if (typeof conf.topic !== "undefined") {
                throw new Exception("The property topic must be a valid topic string.");
            }

            if (typeof conf.select === "string") {
                _select = conf.select;
            }

            if (typeof conf.settings === "object") {
                //TODO more verif
                conf.settings.keys().map(function(key) {
                    _settings[key] = conf.settings[key];
                });
            }
        }

        _hub.requireService({
            component: this,
            contract: window.wisit.shell.ShellService,
            field: "shell"
        }).requireService({
            component: this,
            contract: window.wisit.auth.AuthService,
            field: "auth",
            optional : (typeof conf.auth === "boolean") ? !conf.auth : true
        }).requireService({
            component: this,
            contract: window.wisit.stream.StreamService,
            field: "stream"
        });

        _hub.subscribe(self, _topic, receiveResult);
    };

    function initTerm(term) {
        self.stream.open(function() {
            console.log("[" + self.name + "] WebSocket Open");
            term.echo("[32;1mYou have been properly connected!");
            term.echo();
        }, function() {
            term.error("The connection with the server has been lost...");
            console.log("[" + self.name + "] WebSocket Closed");
        });

        if(term.login_name() !== undefined ){
            term.set_prompt($.terminal.from_ansi("[33;0m"+ term.login_name() + "@wisdom [0m~> "));
        }
    }

    function exit() {
        self.stream.close();

        if(self.auth !== null){
            self.auth.logout();
        }

        //if(typeof _term !== "undefined"){
        //    _term.clear();
        //}
    }

    function interpreter(command, term) {
        var full = command.trim().split(" ");
        var head = full.shift();

        if (head === "") {
            term.flush();
            return;
        }

        if (head === "exit") {
            exit();
            term.logout();
            return;
        }

        if ( self.shell.getCommands().indexOf(head) === -1 ) {
            term.error("unknown command '" + command + "'");
            return;
        }

        self.shell.exec(head, full.join(" "));
    }

    self.start = function() {
        //Use the auth service to login if set in conf, no login otherwise.
        _settings.login = (self.auth !== null) ? self.auth.login : false;

        _settings.onInit = initTerm;
        _settings.onExit = exit;

        _settings.completion = function(term, command, callback) {
            self.shell.autoComplete(term.get_command(),callback);
        };

        _term = $(_select).terminal(interpreter, _settings);
    };

    self.stop = function() {};
}
