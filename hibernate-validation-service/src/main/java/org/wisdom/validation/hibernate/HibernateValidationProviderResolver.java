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
package org.wisdom.validation.hibernate;

import com.google.common.collect.ImmutableList;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;
import java.util.List;

/**
 * Resolves the Hibernate validator.
 * Indeed, the regular validation lookup is not operating right in OSGi environments.
 */
public class HibernateValidationProviderResolver implements ValidationProviderResolver {

    @Override
    public List<ValidationProvider<?>> getValidationProviders() {
        return ImmutableList.<ValidationProvider<?>>of(new HibernateValidator());
    }
}
