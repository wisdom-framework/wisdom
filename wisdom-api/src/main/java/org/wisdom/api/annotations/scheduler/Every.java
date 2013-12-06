package org.wisdom.api.annotations.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to a periodic job.
 * Notice that the period is parsed using the following DSL:
 * <code>
 *     <pre>
 *         1d: every day
 *         1d3h: every day and 3 hours
 *         3h2m1s: every 3 hours 2 minutes and 1 second
 *     </pre>
 * </code>
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
    String value();
}
