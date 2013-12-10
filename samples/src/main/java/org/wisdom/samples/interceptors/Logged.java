package org.wisdom.samples.interceptors;

import org.wisdom.api.annotations.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Just logged requests.
 */
@With(LoggerInterceptor.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logged {

    boolean duration() default true;

}
