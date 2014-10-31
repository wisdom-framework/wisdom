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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation to a periodic job.
 *
 * Notice that the period is parsed using the following DSL:
 * <code>
 *     <pre>
 *         1d: every day
 *         1d3h: every day and 3 hours
 *         3h2m1s: every 3 hours 2 minutes and 1 second
 *     </pre>
 * </code>
 *
 * You can also set the period using the {@code period} and {@code unit} parameters. However if {@code value} is set,
 * these values are ignored.
 *
 * You need to use one way or the other to configure the period.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Every {

    public static final String DAY = "1d";
    public static final String HOUR = "1h";
    public static final String MINUTE = "1m";
    public static final String TWELVE_HOURS = "12h";
    public static final String SIX_HOURS = "6h";

    /**
     * Sets the job period.
     * The job is not executed immediately but after the initial period.
     */
    String value() default "";

    /**
     * Sets the period of time.
     */
    long period() default -1L;

    /**
     * Sets the time unit to use for the period.
     */
    TimeUnit unit() default TimeUnit.MINUTES;



}
