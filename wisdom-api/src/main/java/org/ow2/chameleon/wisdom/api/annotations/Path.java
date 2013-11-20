package org.ow2.chameleon.wisdom.api.annotations;

import org.ow2.chameleon.wisdom.api.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.ow2.chameleon.wisdom.api.Controller;

/**
 * An annotation to declare a general path prepended to all route and web socket declared in the class.
 * <p/>
 * This annotation is retrieved and analyzed at runtime (by the router).
 * Be aware that the path is <strong>NOT</strong> used for routes declared in {@link Controller#routes()}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    String value();

}
