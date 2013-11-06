package org.ow2.chameleon.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to inject a request attribute as argument. The type is inferred from the argument type.
 * This annotation can also be used to inject a {@link org.ow2.chameleon.wisdom.api.http.FileItem} instance. To
 * achieve this the parameter type must be a File Item, and the specified value the field name set in the upload form.
 * <p/>
 * This annotation is retrieved and analyzed at runtime (by the router).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {

    /**
     * The attribute name.
     */
    String value();

}
