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

import org.junit.Test;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.framework.filters.BalancerMember;
import org.wisdom.framework.filters.DefaultBalancerMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultBalancerMemberTest {

    @Test
    public void testInstantiationFromConfiguration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getOrDie("name")).thenReturn("member");
        when(configuration.getOrDie("proxyTo")).thenReturn("http://perdu.com");
        when(configuration.getOrDie("balancerName")).thenReturn("balancer");

        BalancerMember member = new DefaultBalancerMember(configuration);

        assertThat(member.getName()).isEqualTo("member");
        assertThat(member.proxyTo()).isEqualTo("http://perdu.com");
        assertThat(member.getBalancerName()).isEqualTo("balancer");
    }

}