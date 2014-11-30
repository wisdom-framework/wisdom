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

import org.wisdom.api.configuration.Configuration;

/**
 * An implementation of {@link org.wisdom.framework.filters.BalancerMember}.
 */
public class DefaultBalancerMember implements BalancerMember {

    private final String name;
    private final String proxyTo;
    private final String balancerName;

    public DefaultBalancerMember(String name, String proxyTo, String balancerName) {
        this.name = name;
        this.proxyTo = proxyTo;
        this.balancerName = balancerName;
    }

    public DefaultBalancerMember(Configuration configuration) {
        this(
                configuration.getOrDie("name"),
                configuration.getOrDie("proxyTo"),
                configuration.getOrDie("balancerName")
        );
    }


    /**
     * Gets the member name. Used to identify the member.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the URL where requests are transferred.
     *
     * @return the URL
     */
    @Override
    public String proxyTo() {
        return proxyTo;
    }

    /**
     * Gets the name of balancer managing this member.
     *
     * @return the name of the balancer.
     */
    @Override
    public String getBalancerName() {
        return balancerName;
    }
}
