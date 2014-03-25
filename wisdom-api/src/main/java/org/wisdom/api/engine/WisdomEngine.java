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
package org.wisdom.api.engine;

/**
 * A service interface exposed by the Wisdom engine.
 * This service is used  to retrieve basic information about the engine like the hostname and the ports.
 */
public interface WisdomEngine {

    /**
     * Gets the server hostname listened by the engine.
     * @return the host name.
     */
    public String hostname();

    /**
     * Gets the HTTP port listened by the engine.
     * @return the http port, {@literal -1} if not listened
     */
    public int httpPort();

    /**
     * Gets the HTTPS port listened by the engine.
     * @return the https port, {@literal -1} if not listened
     */
    public int httpsPort();

}
