package org.ow2.chameleon.wisdom.api;

import org.ow2.chameleon.wisdom.api.cookies.FlashCookie;
import org.ow2.chameleon.wisdom.api.cookies.SessionCookie;
import org.ow2.chameleon.wisdom.api.http.*;
import org.ow2.chameleon.wisdom.api.route.Route;

import java.util.Collections;
import java.util.List;

/**
 * Controller super-class.
 */
public abstract class Controller extends Results implements Status, HeaderNames {

    /**
     * Returns the current HTTP context.
     */
    public Context context() {
        Context ctxt =  Context.context.get();
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
     * Must be overridden
     * @return
     */
    public List<Route> routes() {
        return Collections.emptyList();
    }
}
