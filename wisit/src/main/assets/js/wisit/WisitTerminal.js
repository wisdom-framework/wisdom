/* global $, Exception*/

/**
 *
 * @class WisitShellComp
 * @extends HUBU.AbstractComponent
 */

function WisitTerminal() {
    "use strict";

    var self = this;
    var _hub;

    var _commands = null;
    var _topic = "/wisit/stream";
    var _term;
    var _select = "#wisit";

    var _binded = 0;

    var _settings = {
        greetings: "                                                  \n"+
                   "      (@_                               _@)\n"+
                   "   \\\\\\_\\   WISDOM INTERACTIVE TERMINAL   /_///\n"+
                   "   <____)                               (____>\n"+
                   "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n",
        width: "100%",
        height: "100%",
        checkArity: false,
        prompt: "wisit@wisdom>",
        onBlur: function() {
            // the height of the body is only 2 lines initialy
            return false;
        },
        tabcompletion: false
    };


    var auth = null; //AuthService
    var shell = null; //ShellService
    var stream = null; //StreamService

    self.name = "WisitTerminal";

    function receiveResult(data) {
        if (typeof data.result === "string") {
            _term.echo(data.result);
        }
        if (typeof data.err === "string") {
            _term.error(data.err);
        }
    }

    //
    // Loosy worked around to avoid starting without the dependencies
    // Waiting for h-ubu lifecycle to be fixed
    //

    function unbind() {
        _binded--;
        self.stop();
    }

    function bindAuth(authService) {
        auth = authService;
        _binded++;
        self.start();
    }

    function bindShell(shellService) {
        shell = shellService;
        _binded++;
        self.start();
    }

    function bindStream(streamService) {
        stream = streamService;
        _binded++;
        self.start();
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
     * @param {topic} [conf.topic="/wisit/stream"] - The topic on which command result are publish!
     */
    self.configure = function(theHub, conf) {
        _hub = theHub;

        if (typeof conf !== "undefined") {
            //If the property `topic` has been define, check if valid and use it.
            if ((typeof conf.topic === "string") && conf.topic.match(/^\/\w+(\/\w+)*$/)) {
                _topic = conf.topic;
            } else if (typeof conf.topic !== undefined) {
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
            contract: window.wisit.auth.AuthService,
            aggregate: false,
            bind: bindAuth,
            unbind: unbind
        });

        _hub.requireService({
            component: this,
            contract: window.wisit.stream.StreamService,
            aggregate: false,
            bind: bindStream,
            unbind: unbind
        });

        _hub.requireService({
            component: this,
            contract: window.wisit.shell.ShellService,
            aggregate: false,
            bind: bindShell,
            unbind: unbind
        });

        _hub.subscribe(self, _topic, receiveResult);
    };

    function initTerm(term) {
        _commands = shell.getCommands();

        stream.open(function() {
            console.log("["+self.name+"] WebSocket Open");
        }, function() {
            console.log("["+self.name+"] WebSocket Closed");
        });

        term.echo("Hello "+term.login_name()+"!");
        term.set_prompt(term.login_name()+"@wisdom>");
    }

    function exit() {
        auth.logout();
        stream.close();
        _commands = null;
    }

    function interpreter(command, term) {
        var full = command.trim().split(" ");
        var head = full.shift();

        if(head === ""){
            term.flush();
            return;
        }

        if (head === "exit") {
            exit();
            term.logout();
            return;
        }

        if (_commands.indexOf(head) === -1) {
            term.error("unknown command '" + command + "'");
            return;
        }
        
        shell.exec(head, full.join(" "));
    }

    self.start = function() {
        if (_binded !== 3) {
            return;
        }

        _settings.login = auth.login;
        _settings.onInit = initTerm;
        _settings.onExit = exit;
        _settings.exit = false;

        _term = $(_select).terminal(interpreter, _settings);
    };

    self.stop = function() {};
}
