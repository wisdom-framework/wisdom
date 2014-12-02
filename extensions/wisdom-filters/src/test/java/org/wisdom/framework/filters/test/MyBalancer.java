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

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Unbind;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.interception.Filter;
import org.wisdom.framework.filters.BalancerFilter;
import org.wisdom.framework.filters.BalancerMember;

@Service
public class MyBalancer extends BalancerFilter implements Filter {

    @Override
    public String getName() {
        return "balancer";
    }

    @Override
    protected String getPrefix() {
        return "/balancer";
    }

    @Bind(aggregate = true, optional = true)
    public void bindMember(BalancerMember member) {
        addMember(member);
    }

    @Unbind
    public void unbindMember(BalancerMember member) {
        removeMember(member);
    }
}
