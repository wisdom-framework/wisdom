package org.ow2.chameleon.wisdom.validation;

import org.hibernate.validator.HibernateValidator;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 06/11/2013
 * Time: 19:01
 * To change this template use File | Settings | File Templates.
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
