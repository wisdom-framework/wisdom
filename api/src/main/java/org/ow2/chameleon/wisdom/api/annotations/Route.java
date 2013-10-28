package org.ow2.chameleon.wisdom.api.annotations;

import org.ow2.chameleon.wisdom.api.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to declare the route of an action method.
 * <p/>
 * This annotation is retrieved and analyzed at runtime (by the router).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    /**
     * The method of the route.
     */
    HttpMethod method();

    /**
     * The route's uri, with placeholders, such as:
     * <code>
     *     <pre>
     *         /foo/{id}
     *         /foo/{id}/{name}
     *         /foo/{id<[0-9]+>}
     *         /foo/{path*}
     *     </pre>
     * </code>
     */
    String uri();

}
