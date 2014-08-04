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
package org.wisdom.api.content;

import com.fasterxml.jackson.databind.Module;

/**
 * A service exposed by the content manager to let applications register custom JSON serializer and deserializer
 * (also call module).
 * <p/>
 * Users must not forget to call {@link #unregister(com.fasterxml.jackson.databind.Module)} for each module they
 * registered.
 */
public interface JacksonModuleRepository {

    /**
     * Registers a module.
     * Don't forget to unregister the module when leaving.
     *
     * @param module the module
     */
    public void register(Module module);

    /**
     * Un-registers a module.
     *
     * @param module the module
     */
    public void unregister(Module module);

}
