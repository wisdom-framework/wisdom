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

import java.util.concurrent.Callable;

@Component
@Provides
@Instantiate
public class AkkaBootstrap implements AkkaSystemService {

    private final BundleContext context;
    private ServiceRegistration<ActorSystem> systemRegistration;
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
        system.shutdown();
    }

    private void unregisterQuietly(ServiceRegistration<?> registration) {
        try {
            registration.unregister();
        } catch (Exception e) {
            // Ignored.
        }
    }

    @Override
    public ActorSystem system() {
        return system;
    }

    @Override
    public Future<Result> dispatch(Callable<Result> callable, Context context) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), context, Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public Future<Result> dispatch(Callable<Result> callable) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), Context.context.get(),
                        Thread.currentThread().getContextClassLoader
                                ()));
    }

    public ExecutionContext fromThread() {
        return new HttpExecutionContext(system.dispatcher(), Context.context.get(),
                Thread.currentThread().getContextClassLoader());
    }
}
