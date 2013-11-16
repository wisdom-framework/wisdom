package org.ow2.chameleon.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method receiving a web socket event.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnMessage {

    /**
     * The uri of the web sockets. As for route this uri can take contain placeholders:
     * <code>
     *     <pre>
     *         /foo/{id}
     *         /foo/{id}/{name}
     *         /foo/{id<[0-9]+>}
     *         /foo/{path*}
     *     </pre>
     * </code>
     * As a consequence, the identify callbacks can be notified for several web sockets. The method has to track the
     * open / close events to detect when all sockets are closed.
     */
    String value();
}
