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

/**
 * The interface exposed by 'proxies' willing to participate to a balance strategy.
 * Each balancer defines its own group (i.e. the name of the balancer), and so members have to tell by which balancer
 * they are managed.
 */
public interface BalancerMember {

    /**
     * Gets the member name. Used to identify the member.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the URL where requests are transferred.
     *
     * @return the URL
     */
    String proxyTo();

    /**
     * Gets the name of balancer managing this member.
     *
     * @return the name of the balancer.
     */
    String getBalancerName();

}
