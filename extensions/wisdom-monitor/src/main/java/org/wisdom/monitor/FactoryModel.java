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
package org.wisdom.monitor;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents factories.
 */
public class FactoryModel {

    public static List<FactoryModel> factories(BundleContext context) {
        List<FactoryModel> factories = new ArrayList<FactoryModel>();
        try {
            for (ServiceReference ref : context.getServiceReferences(Factory.class, null)) {
                factories.add(new FactoryModel((Factory) context.getService(ref)));
            }
            for (ServiceReference ref : context.getServiceReferences(HandlerFactory.class, null)) {
                factories.add(new FactoryModel((Factory) context.getService(ref)));
            }
        } catch (InvalidSyntaxException e) {
            // Ignore it.
        }
        return factories;
    }

    private final Factory factory;

    public FactoryModel(Factory factory) {
        this.factory = factory;
    }

    public String getName() {
        if (factory.getVersion() == null) {
            return factory.getName();
        } else {
            return factory.getName() + " - " + factory.getVersion();
        }
    }

    public boolean isHandler() {
        return factory instanceof HandlerFactory;
    }

    public String getArchitecture() {
        return factory.getDescription().toString();
    }

    public String getHandlerName() {
        if (factory instanceof HandlerFactory) {
            return ((HandlerFactory) factory).getHandlerName();
        }
        return null;
    }

    public String getState() {
        switch (factory.getState()) {
            case Factory.INVALID:
                return "INVALID";
            case Factory.VALID:
                return "VALID";
        }
        return null;
    }

}
