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
package org.wisdom.monitor.extensions.osgi;

import org.osgi.framework.Bundle;

/**
 * Handles bundle states.
 */
public class BundleStates {

    public static final String ACTIVE = "ACTIVE";
    public static final String INSTALLED = "INSTALLED";
    public static final String RESOLVED = "RESOLVED";
    public static final String STARTING = "STARTING";
    public static final String STOPPING = "STOPPING";
    public static final String UNINSTALLED = "UNINSTALLED";

    private BundleStates() {
        // Avoid direct instantiation.
    }

    public static String from(int state) {
        switch (state) {
            case Bundle.ACTIVE:
                return ACTIVE;
            case Bundle.INSTALLED:
                return INSTALLED;
            case Bundle.RESOLVED:
                return RESOLVED;
            case Bundle.STARTING:
                return STARTING;
            case Bundle.STOPPING:
                return STOPPING;
            case Bundle.UNINSTALLED:
                return UNINSTALLED;
            default:
                return Integer.toString(state);
        }
    }

    public static String from(Bundle bundle) {
        return from(bundle.getState());
    }

    public static int from(String state) {
        switch (state) {
            case ACTIVE:
                return Bundle.ACTIVE;
            case INSTALLED:
                return Bundle.INSTALLED;
            case RESOLVED:
                return Bundle.RESOLVED;
            case STARTING:
                return Bundle.STARTING;
            case STOPPING:
                return Bundle.STOPPING;
            case UNINSTALLED:
                return Bundle.UNINSTALLED;
            default:
                return -1; // Unknown.
        }
    }

}
