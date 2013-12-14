package org.wisdom.api.cache;

import org.wisdom.api.annotations.Interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an action to be cached on server side using the Cache Service.
 */
@Interception
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {
    /**
     * The cache key to store the result in
     */
    String key();

    /**
     * The duration the action should be cached for.  Defaults to 0.
     */
    int duration() default 0;
}