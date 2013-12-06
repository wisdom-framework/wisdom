package org.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to inject a request parameter as argument. It can be a parameter from the query part of the request
 * or from the path.
 * The type is inferred from the argument type.
 * <p/>
 * This annotation is retrieved and analyzed at runtime (by the router).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

    /**
     * The parameter name.
     */
    String value();

}
