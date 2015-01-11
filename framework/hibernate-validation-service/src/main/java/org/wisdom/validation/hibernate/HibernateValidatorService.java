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

import org.apache.felix.ipojo.annotations.*;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.ProviderSpecificBootstrap;

/**
 * Exports the Hibernate Validator as an OSGi service.
 */
@Component(immediate = true)
@Instantiate
public class HibernateValidatorService {

    private final BundleContext context;
    private ServiceRegistration<Validator> registration;
    private ServiceRegistration<ValidatorFactory> factoryRegistration;

    @Requires
    ConstraintMessageInterpolator interpolator;

    /**
     * Creates an instance of {@link org.wisdom.validation.hibernate.HibernateValidatorService}.
     * For testing purpose only.
     */
    public HibernateValidatorService() {
        this(null);
    }

    /**
     * Creates an instance of {@link org.wisdom.validation.hibernate.HibernateValidatorService}.
     *
     * @param context the bundle context
     */
    public HibernateValidatorService(BundleContext context) {
        this.context = context;
    }

    /**
     * Initializes the validator, and registers it as an OSGi service (if the bundle context is set).
     *
     * @return the validator.
     */
    @Validate
    public Validator initialize() {
        // configure and build an instance of ValidatorFactory
        ProviderSpecificBootstrap<HibernateValidatorConfiguration> validationBootStrap = javax.validation.Validation
                .byProvider(HibernateValidator.class);

        // bootstrap to properly resolve in an OSGi environment
        validationBootStrap.providerResolver(new HibernateValidationProviderResolver());

        HibernateValidatorConfiguration configure = validationBootStrap.configure().messageInterpolator(interpolator);
        interpolator.setDefaultInterpolator(configure.getDefaultMessageInterpolator());

        // now that we've done configuring the ValidatorFactory, let's build it
        ValidatorFactory validatorFactory = configure.buildValidatorFactory();

        // retrieve a unique validator.
        Validator validator = validatorFactory.getValidator();

        // Register the validator.
        if (context != null) {
            registration = context.registerService(Validator.class, new WrappedValidator(validator), null);
            factoryRegistration = context.registerService(ValidatorFactory.class, validatorFactory, null);
        }

        return validator;
    }

    /**
     * Unregisters the validator service.
     */
    @Invalidate
    public void tearDown() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        if (factoryRegistration != null) {
            factoryRegistration.unregister();
            factoryRegistration = null;
        }
    }
}
