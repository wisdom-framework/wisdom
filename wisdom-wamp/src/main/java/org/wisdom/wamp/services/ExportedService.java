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
package org.wisdom.wamp.services;

import java.util.Map;

/**
 * A reference on a service exported using WAMP.
 */
public class ExportedService {

    /**
     * The service object.
     */
    public final Object service;

    /**
     * Properties.
     */
    public final Map<String, Object> properties;

    /**
     * The endpoint url.
     */
    public final String url;

    public ExportedService(Object service, Map<String, Object> properties, String url) {
        if (service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }
        if (url == null  || url.isEmpty()) {
            throw new IllegalArgumentException("url cannot be null or empty");
        }
        this.service = service;
        this.properties = properties;
        this.url = url;
    }
}
