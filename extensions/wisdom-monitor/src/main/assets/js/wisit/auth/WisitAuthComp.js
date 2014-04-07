/* global $, Exception*/

/**
 *
 * Implementation of the {@link window.wisit.auth.AuthService}
 *
 * @class WisitAuthComp
 * @extends HUBU.AbstractComponent
 * @extends window.wisit.auth.AuthService
 */

function WisitAuthComp() {
    "use strict";

    var self = this;
    var _hub;

    var _root = "/wisit";
    var _logged = false;

    self.name = "WisitAuthComp";

    self.getComponentName = function() {
        return self.name;
    };

    /**
     * Configure the instance of the WisitAuthComp component.
     *
     * @method configure
     * @param {HUBU.hub} theHub
     * @param conf - The AuthService configuration.
     * @param {root} [conf.root="/wisit"] - The url root of the wisit login resource (WisitLoginController)
     */
    self.configure = function(theHub, conf) {
        _hub = theHub;

        if (typeof conf !== "undefined") {
            if ((typeof conf.root === "string") && conf.root.match(/^\/\w+(\/\w+)*$/)) {
                _root = conf.root;
            } else if (typeof conf.root !== "undefined") {
                throw new Exception("The property root must be a valid path string.");
            }
        }

        //register the RactiveRenderService
        _hub.provideService({
            component: self,
            contract: window.wisit.auth.AuthService,
            properties: {
                root: _root
            }
        });
    };

    self.start = function() {};

    self.stop = function() {
        if (_logged) {
            self.logout();
        }
    };

    self.login = function(user, pass, callback) {
        var auth = {
            user: user,
            pass: pass
        };

        return $.ajax({
            url: _root + "/login",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(auth),
            async: false,
            statusCode: {
                200: function(token) {
                    _logged = true;
                    callback(token);
                }
            }
        }).fail(function(xhr, status, error) {
            callback(false, status, error);
        });
    };

    self.logout = function(success) {
        return $.get(_root + "/logout", function() {
            if(typeof success === "function"){
              success();
            }
            _logged = false;
        });
    };
}
