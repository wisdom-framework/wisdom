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
package org.wisdom.api.annotations.scheduler;

import org.wisdom.api.annotations.Interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation marking an action method to be executed asynchronously. It let you define a timeout. If the result
 * is not processed before the timeout is reached, an error is returned to the client (Gateway Timeout).
 */
@Interception
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

    /**
     * Sets the amount of time to wait for processing. If this amount is reached, an error result is returned to
     * the client.
     */
    long timeout() default 0L;

    /**
     * Sets the time unit to use for the period. Meaningful only if the period is defined.
     * The default unit is <em>SECONDS</em>.
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
