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
package controllers.loggers;

import org.apache.commons.logging.Log;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.File;
import java.util.logging.Logger;

@Controller
public class LogController extends DefaultController {

    @Requires
    ApplicationConfiguration configuration;

    @Route(method = HttpMethod.GET, uri="/log")
    public Result log() {
        return ok(new File(configuration.getBaseDir(), "logs/wisdom.log")).as(MimeTypes.TEXT);
    }

    @Route(method = HttpMethod.GET, uri = "/log/slf4j")
    public Result slf4j(@Parameter("message") String message) {
        logger().error(message);
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/log/jul")
    public Result jul(@Parameter("message") String message) {
        final Logger logger = Logger.getLogger(LogController.class.getName());
        logger.severe(message);
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/log/jcl")
    public Result jcl(@Parameter("message") String message) {
        final Log log = org.apache.commons.logging.LogFactory.getLog(LogController.class.getName());
        log.error(message);
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/log/log4j")
    public Result log4j(@Parameter("message") String message) {
        org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogController.class.getName());
        log.error(message);
        return ok();
    }



}
