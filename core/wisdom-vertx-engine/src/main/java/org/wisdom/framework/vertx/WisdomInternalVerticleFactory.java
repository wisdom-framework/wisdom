/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class WisdomInternalVerticleFactory implements VerticleFactory {

    private final List<Server> servers;
    private final ServiceAccessor accessor;

    public WisdomInternalVerticleFactory(ServiceAccessor accessor, List<Server> servers) {
        this.accessor = accessor;
        this.servers = servers;
    }

    /**
     * @return "wisdom-internal"
     */
    @Override
    public String prefix() {
        return "wisdom-internal";
    }

    @Override
    public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
        return new WisdomServiceVerticle(accessor, servers);
    }
}
