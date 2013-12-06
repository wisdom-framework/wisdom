package org.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a controller.
 *
 * This annotation is handled by the iPOJO manipulator and is equivalent to:
 * <code>
 *     <pre>
 *          @Component
 *          @Provides(specifications = Controller.class)
 *          @Instantiate
 *     </pre>
 * </code>
 *
 * As this annotation is read during the manipulation process, the <em>class</em> retention (default) is enough.
 */
@Target(ElementType.TYPE)
public @interface Controller {
}
