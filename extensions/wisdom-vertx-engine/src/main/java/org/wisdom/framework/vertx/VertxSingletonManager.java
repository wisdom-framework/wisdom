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
package org.wisdom.framework.vertx;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultVertxFactory;

@Component
@Instantiate
public class VertXSingletonManager {

    @Context
    private BundleContext context;

    private Vertx vertx;
    private ServiceRegistration<Vertx> reg;

    @Validate
    public void start() {
        DefaultVertxFactory factory = new DefaultVertxFactory();
        vertx = factory.createVertx();

        reg = context.registerService(Vertx.class, vertx, null);
    }

    @Invalidate
    public void stop() {
        unregisterQuietly(reg);
        vertx.stop();
        vertx = null;
    }

    private static void unregisterQuietly(ServiceRegistration reg) {
        if (reg != null) {
            try {
                reg.unregister();
            } catch (IllegalStateException e) {
                // Ignore it.
            }
        }
    }
}
