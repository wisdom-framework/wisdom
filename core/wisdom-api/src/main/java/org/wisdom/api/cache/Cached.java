/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.api.cache;

import org.wisdom.api.annotations.Interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an action to be cached on server side using the Cache Service.
 * So the result of the action is stored in the cache service for the specified time. All requests on this action
 * reuse this stored result (until it is invalidated).
 */
@Interception
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {

    /**
     * The cache key to store the result in. Default to the request's URI. Be aware that if several action use the
     * same uri, you must configure the key to avoid collisions.
     */
    String key() default "";

    /**
     * The duration the action should be cached for (in second).  Defaults to 0 corresponding to 365 days.
     */
    int duration() default 0;

}