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
package org.wisdom.monitor.extensions.ipojo;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents factories.
 * Object from this class are consumed by the template.
 */
public class FactoryModel {

    /**
     * Creates a list of factory model from the factory exposed. These factories are retrieved from the bundle context.
     *
     * @param context the context
     * @return the list of factory model
     */
    public static List<FactoryModel> factories(BundleContext context) {
        List<FactoryModel> factories = new ArrayList<FactoryModel>();
        try {
            for (ServiceReference ref : context.getServiceReferences(Factory.class, null)) {
                factories.add(new FactoryModel((Factory) context.getService(ref)));
            }
            for (ServiceReference ref : context.getServiceReferences(HandlerFactory.class, null)) {
                factories.add(new FactoryModel((Factory) context.getService(ref)));
            }
        } catch (InvalidSyntaxException e) { //NOSONAR
            // Ignore it.
        }
        return factories;
    }

    private final Factory factory;

    /**
     * Creates the factory model from the given factory object.
     *
     * @param factory the model
     */
    protected FactoryModel(Factory factory) {
        this.factory = factory;
    }

    /**
     * @return the factory name (either name of name - version).
     */
    public String getName() {
        if (factory.getVersion() == null) {
            return factory.getName();
        } else {
            return factory.getName() + " - " + factory.getVersion();
        }
    }

    /**
     * @return whether or not the factory is a handler factory.
     */
    public boolean isHandler() {
        return factory instanceof HandlerFactory;
    }

    /**
     * @return the factory raw architecture.
     */
    public String getArchitecture() {
        return factory.getDescription().toString();
    }

    /**
     * @return the handler name if the factory is a handler factory.
     */
    public String getHandlerName() {
        if (factory instanceof HandlerFactory) {
            return ((HandlerFactory) factory).getHandlerName();
        }
        return null;
    }

    /**
     * @return the string representation of the factory state.
     */
    public String getState() {
        if (factory.getState() == Factory.INVALID) {
            return "INVALID";
        } else {
            return "VALID";
        }
    }

}
