package org.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a requirements on a Crud service.
 *
 * This annotation is handled by the iPOJO manipulator and is equivalent to:
 * <code>
 *     <pre>
 *          @Requires(filter="(entity.classname=...)"
 *     </pre>
 * </code>
 * The mapped requirement is scalar and mandatory.
 * As this annotation is read during the manipulation process, the <em>class</em> retention (default) is enough.
 */
@Target(ElementType.FIELD)
public @interface Model {

    /**
     * Specified the entity class.
     */
    Class<?> value();
}
