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

    var _root = "/wisit";
    var _topic = "/wisit/stream";
    var _commands = null;

    self.name = "WisitShellComp";

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
            } else if (typeof conf.root !== undefined) {
                throw new Exception("The property root must be a valid path string.");
            }

            if ((typeof conf.topic === "string") && conf.topic.match(/^\/\w+(\/\w+)*$/)) {
                _topic = conf.topic;
            } else if (typeof conf.topic !== undefined) {
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

    self.start = function() {};

    self.stop = function() {
        _commands = null;
    };

    self.getCommands = function() {
        $.ajax({
            url: _root + "/command",
            type: "GET",
            async: false,
            dataType: "json",
            success: function(commandList) {
                _commands = commandList;
            }
        }).fail(function(xhr, status, error) {
            //TODO log
        });

        return _commands;
    };

    self.exec = function(cmd, args) {
        return $.ajax({
            url: _root + "/command/" + cmd,
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(args)
        }).done(function(data) {
            if (typeof data !== "undefined") {
                _hub.publish(self, _topic, {data: data}); //Publish the command result if any
            }
        }).fail(function(xhr, status, error) {
            //TODO log
        });
    };

    self.getTopic = function() {
        return _topic;
    };
}
