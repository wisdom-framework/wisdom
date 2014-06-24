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
package org.wisdom.monitor.extensions.dashboard;

/**
 * Represents the result of a health check. A health check denotes the status of a part of the system. When a health
 * check returns a KO state, the system does not operate correctly.
 */
class HealthState {

    /**
     * Is the state OK or KO.
     */
    public final boolean ok;

    /**
     * The error associated with a KO state.
     */
    public final Exception error;

    /**
     * Creates a new OK Health State instance.
     *
     * @return the new instance.
     */
    public static HealthState ok() {
        return new HealthState(true, null);
    }

    /**
     * Creates a new KO Health State instance, without any information about the failure.
     *
     * @return the new instance.
     */
    public static HealthState ko() {
        return new HealthState(false, null);
    }

    /**
     * Creates a new KO Health State instance with an exception attached.
     *
     * @param e the exception
     * @return the new instance.
     */
    public static HealthState ko(Exception e) {
        return new HealthState(false, e);
    }

    /**
     * Creates a new instance of Health State.
     *
     * @param ok    whether or not the health is ok
     * @param error an optional error when the health check is KO.
     */
    HealthState(boolean ok, Exception error) {
        this.ok = ok;
        this.error = error;
    }
}
