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
package org.wisdom.wisit.auth;

/**
 * Wisit Authentication service.
 *
 * @author Jonathan M. Bardin
 */
public interface WisitAuthService {

    /**
     * The Wisit username property (in application.conf).
     */
    String WISIT_USER = "wisit.user";

    /**
     * The Wisit password property (in application.conf).
     */
    String WISIT_PASS = "wisit.pass";

    /**
     * @return true if the logged user is authorised to use the wisdom terminal.
     */
    boolean isAuthorised();
}
