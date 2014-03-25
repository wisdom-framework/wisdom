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
package org.wisdom.wamp;

/**
 * Constants used in the WAMP support.
 */
public interface Constants {

    /**
     * The WAMP protocol version.
     */
    public static final int WAMP_PROTOCOL_VERSION = 1;

    /**
     * The WAMP server id.
     */
    public static final String WAMP_SERVER_VERSION = "Wisdom/Wamp 1.0";

    /**
     * The route to access WAMP.
     */
    public static final String WAMP_ROUTE = "/wamp";

    /**
     * The error url prefix to append to the regular prefix.
     */
    public static final String WAMP_ERROR = "/error";
}
