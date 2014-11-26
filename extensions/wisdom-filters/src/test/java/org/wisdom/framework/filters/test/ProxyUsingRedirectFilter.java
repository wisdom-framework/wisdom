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
package org.wisdom.framework.filters.test;

import org.wisdom.api.annotations.Service;
import org.wisdom.api.interception.Filter;
import org.wisdom.framework.filters.ProxyFilter;

import java.util.regex.Pattern;

@Service
public class ProxyUsingRedirectFilter extends ProxyFilter implements Filter {

    @Override
    protected String getProxyTo() {
        return "http://httpbin.org/redirect-to?url=http://perdu.com";
    }

    @Override
    protected boolean followRedirect(String method) {
        return true;
    }

    /**
     * Gets the filter priority, determining the position of the filter in the filter chain. Filter with a high
     * priority are called first. Notice that the router are caching these priorities and so cannot changed.
     * <p>
     * It is heavily recommended to allow configuring the priority from the Application Configuration.
     *
     * @return the priority
     */
    @Override
    public int priority() {
        return 1000;
    }

    @Override
    protected String getPrefix() {
        return "/proxy/redirect";
    }
}
