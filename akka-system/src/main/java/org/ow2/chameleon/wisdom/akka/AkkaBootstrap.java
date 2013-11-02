package org.ow2.chameleon.wisdom.akka;

import akka.actor.ActorSystem;
import akka.osgi.OsgiActorSystemFactory;
import com.typesafe.config.ConfigFactory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Component(immediate = true)
@Instantiate
public class AkkaBootstrap {

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
}
