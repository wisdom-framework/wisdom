/*
 * global window
 */

(function(){
  if(typeof window.wisit === "undefined"){
    window.wisit = {};
  }
  if(typeof window.wisit.auth === "undefined"){
    window.wisit.auth = {};
  }
})();

/**
 *
 * @class wisit.auth.AuthService
 * @global
 * @abstract
 */
window.wisit.auth.AuthService = (function(){
  "use strict";

  var auth = Object.create(null);

  /**
  *
  * jshint unused:false 
  * @method login
  * @memberof wisit.auth.AuthService
  */
  auth.login = function(user,pass,callback){};

  /**
  *
  * jshint unused:false 
  * @method logout
  * @memberof wisit.auth.AuthService
  */
  auth.logout = function(success){};

  return auth;
})();
