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
package org.wisdom.api.router.parameters;

/**
 * The parameter value source.
 */
public enum Source {
    /**
     * A parameter from the query or from the path.
     */
    PARAMETER,

    /**
     * Like {@link #PARAMETER}, but restrict the lookup to the query parameters.
     */
    QUERY,

    /**
     * Like {@link #PARAMETER}, but restrict the lookup to the path parameters.
     */
    PATH,

    /**
     * This source is designed to ease the injection of HTTP related attributes in action methods.
     */
    HTTP,

    /**
     * An attribute from a form.
     */
    FORM,

    /**
     * The payload.
     */
    BODY,

    /**
     * Bean Parameter.
     */
    BEAN

}
