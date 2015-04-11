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
package org.wisdom.api;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.api.templates.Template;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller super-class.
 * This call contains useful method making the development of controller much more fluent and easy.
 */
public class DefaultController extends Results implements Status, HeaderNames, Controller {

    /**
     * The logger usable by the controller.
     */
    private Logger logger; //NOSONAR the logger is attached to the instance.

    /**
     * Retrieves the logger usable by the controller.
     * If the logger does not exist yet, get one.
     *
     * @return the logger.
     */
    public Logger logger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(this.getClass().getName());
        }
        return logger;
    }


    /**
     * @return the current HTTP context.
     */
    public Context context() {
        Context ctxt = Context.CONTEXT.get();
        if (ctxt == null) {
            throw new IllegalStateException("No context set from " + Thread.currentThread().getName());
        }
        return ctxt;
    }

    /**
     * @return the current HTTP request.
     */
    public Request request() {
        return context().request();
    }

    /**
     * @return the current HTTP session.
     */
    public SessionCookie session() {
        return context().session();
    }

    /**
     * Puts a new value into the current session.
     *
     * @param key   the key
     * @param value the value
     */
    public void session(String key, String value) {
        session().put(key, value);
    }

    /**
     * Returns a value from the session.
     *
     * @param key the key
     * @return the stored value
     */
    public String session(String key) {
        return session().get(key);
    }

    /**
     * @return the current HTTP flash scope.
     */
    public FlashCookie flash() {
        return context().flash();
    }

    /**
     * Puts a new value into the flash scope.
     *
     * @param key   the key
     * @param value the value
     */
    public void flash(String key, String value) {
        flash().put(key, value);
    }

    /**
     * Returns a value from the flash scope.
     *
     * @param key the key
     * @return the stored value
     */
    public String flash(String key) {
        return flash().get(key);
    }

    /**
     * Default implementation of the routes method.
     *
     * @return an empty list. The router must also check for the {@link org.wisdom.api.annotations
     * .Route} annotations.
     */
    public List<Route> routes() {
        return Collections.emptyList();
    }

    /**
     * Renders the given template.
     *
     * @param template   the template
     * @param parameters the parameters
     * @return the renderable object.
     */
    public Renderable<?> render(Template template, Map<String, Object> parameters) {
        return template.render(this, parameters);
    }

    /**
     * Renders the given template.
     *
     * @param template   the template
     * @param parameters the parameters given as list following the scheme: key, value, key, value...
     * @return the renderable object.
     */
    public Renderable<?> render(Template template, Object... parameters) {
        Map<String, Object> map = Maps.newHashMap();
        String key = null;
        for (Object parameter : parameters) {
            if (key == null) {
                if (!(parameter instanceof String)) {
                    throw new IllegalArgumentException("The template variable name " + parameter + " must be a string");
                } else {
                    key = (String) parameter;
                }
            } else {
                map.put(key, parameter);
                key = null;
            }
        }
        if (key != null) {
            throw new IllegalArgumentException("Illegal number of parameter, the variable " + key + " has no value");
        }
        return template.render(this, map);
    }

    /**
     * Renders the given template.
     *
     * @param template the template
     * @return the renderable object.
     */
    public Renderable<?> render(Template template) {
        return template.render(this);
    }


}
