/*
 * global window
 */

/**
 *
 * @class wisit.shell.ShellService
 * @global
 * @abstract
 */
window.wisit.shell.ShellService = (function(){
  "use strict";

  var service = Object.create(null);

  /**
  *
  * jshint unused:false
  * @method login
  * @memberof wisit.shell.ShellService
  */
  service.getCommands = function(){};

  /**
  *
  * jshint unused:false
  * @method login
  * @memberof wisit.shell.ShellService
  */
  service.exec = function(cmd,args){};

  /**
  *
  * @method getTopic
  * @memberof wisit.stream.StreamService
  */
  service.getTopic = function(){};

  return service;
})();
