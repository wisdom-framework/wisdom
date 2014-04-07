/*
 * global window
 */

/**
 *
 * @class wisit.stream.StreamService
 * @global
 * @abstract
 */
window.wisit.stream.StreamService = (function(){
  "use strict";

  var service = Object.create(null);

  /**
  *
  * jshint unused:false 
  * @method open
  * @memberof wisit.stream.StreamService
  */
  service.open = function(onOpen,onClose){};

  /**
  *
  * jshint unused:false 
  * @method logout
  * @memberof wisit.stream.StreamService
  */
  service.close = function(){};

  /**
  *
  * @method getTopic
  * @memberof wisit.stream.StreamService
  */
  service.getTopic = function(){};

  return service;
})();
