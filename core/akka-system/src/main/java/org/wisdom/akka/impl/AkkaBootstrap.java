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
package org.wisdom.akka.impl;

import akka.actor.ActorSystem;
import akka.osgi.OsgiActorSystemFactory;
import com.typesafe.config.ConfigFactory;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.io.InputStream;
import java.util.concurrent.Callable;

@Component
@Provides
@Instantiate
public class AkkaBootstrap implements AkkaSystemService {

    private final BundleContext context;
    private ServiceRegistration<ActorSystem> systemRegistration;

    /**
     * The actor system.
     */
    private ActorSystem system;

    public AkkaBootstrap(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void start() {
        OsgiActorSystemFactory osgiActorSystemFactory = OsgiActorSystemFactory.apply(context, ConfigFactory.empty());
        system = osgiActorSystemFactory.createActorSystem("wisdom-system");

        systemRegistration = context.registerService(ActorSystem.class, system, null);
    }

    @Invalidate
    public void stop() {
        unregisterQuietly(systemRegistration);
        systemRegistration = null;
        if (system != null) {
            system.shutdown();
            system = null;
        }
    }

    private void unregisterQuietly(ServiceRegistration<?> registration) {
        if (registration != null) {
            try {
                registration.unregister();
            } catch (Exception e) { //NOSONAR
                // Ignored.
            }
        }
    }

    @Override
    public ActorSystem system() {
        return system;
    }

    @Override
    public Future<Result> dispatchResultWithContext(Callable<Result> callable, Context context) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), context, Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public Future<Result> dispatchResult(Callable<Result> callable) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), Context.CONTEXT.get(),
                        Thread.currentThread().getContextClassLoader())
        );
    }

    @Override
    public Future<InputStream> dispatchInputStream(Callable<InputStream> callable) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), Context.CONTEXT.get(),
                        Thread.currentThread().getContextClassLoader())
        );
    }

    @Override
    public <T> Future<T> dispatch(Callable<T> callable, ExecutionContext ctx) {
        return akka.dispatch.Futures.future(callable, ctx);
    }

    public ExecutionContext fromThread() {
        return new HttpExecutionContext(system.dispatcher(), Context.CONTEXT.get(),
                Thread.currentThread().getContextClassLoader());
    }
}
