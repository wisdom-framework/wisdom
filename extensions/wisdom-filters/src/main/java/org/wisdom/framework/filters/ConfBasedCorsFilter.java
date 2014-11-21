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
package org.wisdom.framework.filters;

import java.util.List;

import org.apache.felix.ipojo.annotations.Controller;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.router.Router;

@Service
public class ConfBasedCorsFilter extends CorsFilter {

    private static final String CORS_FILTER_ALLOW_ORIGIN = "cors.allow-origin";

    private static final String CORS_FILTER_ALLOW_HEADERS = "cors.allow-headers";

    private static final String CORS_FILTER_ENABLED = "cors.enabled";

    private static final String CORS_FILTER_ALLOW_CREDENTIALS = "cors.allow-credentials";

    private static final String CORS_FILTER_MAX_AGE = "cors.max-age";

    @Controller
    private boolean active;

    @Requires
    private ApplicationConfiguration configuration;

    private List<String> extraHeaders;

    private List<String> allowedHosts;

    private boolean allowCredentials;

    private Integer preflightMaxAge;

    public ConfBasedCorsFilter(@Requires Router router) {
        super(router);
    }

    @Validate
    public void activate() {
        active = configuration.getBooleanWithDefault(CORS_FILTER_ENABLED, false);
        extraHeaders = configuration.getList(CORS_FILTER_ALLOW_HEADERS);
        allowedHosts = configuration.getList(CORS_FILTER_ALLOW_ORIGIN);
        allowCredentials = configuration.getBooleanWithDefault(CORS_FILTER_ALLOW_CREDENTIALS, false);
        if (configuration.asMap().containsKey(CORS_FILTER_MAX_AGE))
            preflightMaxAge = configuration.getIntegerWithDefault(CORS_FILTER_MAX_AGE, 3600);

    }

    @Override
    public List<String> getExposedHeaders() {
        return extraHeaders;
    }

    @Override
    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    @Override
    public boolean getAllowCredentials() {
        return allowCredentials;
    }

    @Override
    public Integer getMaxAge() {
        return preflightMaxAge;
    }

}
