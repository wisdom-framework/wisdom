package org.wisdom.validation.hibernate;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.content.JacksonModuleRepository;

/**
 *
 */
@Component(immediate = true)
@Instantiate
public class HibernateValidatorModule {

    private final Bundle bundle;
    private final SimpleModule module;
    @Requires
    private JacksonModuleRepository repository;

    public HibernateValidatorModule(BundleContext context) {
        bundle = context.getBundle();
        module = new SimpleModule("Hibernate-Validator-Module", version());
        module.addSerializer(new ConstraintViolationSerializer());
    }

    @Validate
    public void start() {
        repository.register(module);
    }

    @Invalidate
    public void stop() {
        repository.unregister(module);
    }

    public Version version() {
        return new Version(bundle.getVersion().getMajor(), bundle.getVersion().getMinor(),
                bundle.getVersion().getMicro(), null, null, null);
    }
}
