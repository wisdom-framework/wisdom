package org.wisdom.validation.hibernate;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
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

    public HibernateValidatorService() {
        this.context = null;
    }

    public HibernateValidatorService(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void initialize() {
        // configure and build an instance of ValidatorFactory
        ProviderSpecificBootstrap<HibernateValidatorConfiguration> validationBootStrap = javax.validation.Validation
                .byProvider(HibernateValidator.class);

        // bootstrap to properly resolve in an OSGi environment
        validationBootStrap.providerResolver(new HibernateValidationProviderResolver());

        HibernateValidatorConfiguration configure = validationBootStrap.configure();

        // now that we've done configuring the ValidatorFactory, let's build it
        ValidatorFactory validatorFactory = configure.buildValidatorFactory();

        // retrieve a unique validator.
        Validator validator = validatorFactory.getValidator();

        // Register the validator.
        if (context != null) {
            registration = context.registerService(Validator.class, new WrappedValidator(validator), null);
        }
    }

    @Invalidate
    public void tearDown() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
