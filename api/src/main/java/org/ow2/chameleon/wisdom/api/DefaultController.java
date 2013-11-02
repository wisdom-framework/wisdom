package org.ow2.chameleon.wisdom.api;

import com.google.common.collect.Maps;
import org.ow2.chameleon.wisdom.api.cookies.FlashCookie;
import org.ow2.chameleon.wisdom.api.cookies.SessionCookie;
import org.ow2.chameleon.wisdom.api.http.*;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.templates.Template;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Controller super-class.
 */
public abstract class DefaultController extends Results implements Status, HeaderNames, Controller {

    /**
     * Returns the current HTTP context.
     */
    public Context context() {
        Context ctxt = Context.context.get();
        if (ctxt == null) {
            throw new IllegalStateException("No context set from " + Thread.currentThread().getName());
        }
        return ctxt;
    }

    /**
     * Returns the current HTTP request.
     */
    public Request request() {
        return context().request();
    }

    /**
     * Returns the current HTTP response.
     */
    public Response response() {
        return context().response();
    }

    /**
     * Returns the current HTTP session.
     */
    public SessionCookie session() {
        return context().session();
    }

    /**
     * Puts a new value into the current session.
     */
    public void session(String key, String value) {
        session().put(key, value);
    }

    /**
     * Returns a value from the session.
     */
    public String session(String key) {
        return session().get(key);
    }

    /**
     * Returns the current HTTP flash scope.
     */
    public FlashCookie flash() {
        return context().flash();
    }

    /**
     * Puts a new value into the flash scope.
     */
    public void flash(String key, String value) {
        flash().put(key, value);
    }

    /**
     * Returns a value from the flash scope.
     */
    public String flash(String key) {
        return flash().get(key);
    }

    /**
     * Default implementation of the routes method.
     * Returns an empty list. The router must also check for the {@link org.ow2.chameleon.wisdom.api.annotations
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
    public Renderable render(Template template, Map<String, Object> parameters) {
        return template.render(this, parameters);
    }

    /**
     * Renders the given template.
     *
     * @param template   the template
     * @param parameters the parameters given as list following the scheme: key, value, key, value...
     * @return the renderable object.
     */
    public Renderable render(Template template, Object... parameters) {
        Map<String, Object> map = Maps.newHashMap();
        String key = null;
        for (int i = 0; i < parameters.length; i++) {
            if (key == null) {
                if (! (parameters[i] instanceof String)) {
                    throw new IllegalArgumentException("The template variable name " + key + " must be a string");
                } else {
                    key = (String) parameters[i];
                }
            } else {
                map.put(key, parameters[i]);
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
    public Renderable render(Template template) {
        return template.render(this);
    }


}
