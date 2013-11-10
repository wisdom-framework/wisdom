package org.ow2.chameleon.wisdom.validation;

import org.hibernate.validator.HibernateValidator;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the Hibernate validator.
 * Indeed, the regular validation lookup is not operating right in OSGi environments.
 */
public class HibernateValidationProviderResolver implements ValidationProviderResolver {

    @Override
    public List<ValidationProvider<?>> getValidationProviders() {
        List<ValidationProvider<?>> providers = new ArrayList<ValidationProvider<?>>(
                1);
        providers.add(new HibernateValidator());
        return providers;
    }
}
