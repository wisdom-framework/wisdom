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
* Represents the result of a health check.
*/
class HealthState {
    public final boolean ok;
    public final Exception error;

    public static HealthState ok() {
        return new HealthState(true, null);
    }

    public static HealthState ko() {
        return new HealthState(false, null);
    }

    public static HealthState ko(Exception e) {
        return new HealthState(false, e);
    }

    HealthState(boolean ok, Exception error) {
        this.ok = ok;
        this.error = error;
    }
}
