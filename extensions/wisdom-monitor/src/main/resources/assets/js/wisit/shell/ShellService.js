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
  * @method getCommands
  * @memberof wisit.shell.ShellService
  */
  service.getCommands = function(){};

  /**
  *
  * jshint unused:false
  * @method exec
  * @memberof wisit.shell.ShellService
  */
  service.exec = function(cmd,args){};

  /**
  *
  * jshint unused:false
  * @method getTopic
  * @memberof wisit.shell.ShellService
  */
  service.getTopic = function(){};

  /**
  *
  * @method autoComplete
  * @memberof wisit.shell.ShellService
  */
  service.autoComplete = function(fullcommand, callback) {};

  return service;
})();
