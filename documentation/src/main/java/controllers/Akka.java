package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.akka.AkkaSystemService;
import scala.concurrent.duration.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrate how to access Akka.
 */
public class Akka {
    // tag::akka[]
    @Requires
    AkkaSystemService akka;

    public void doSomethingWithAkka() {
        // Retrieve the default actor system
        ActorSystem system = akka.system();
    }
    // end::akka[]

    ActorRef receiver = null;

    // tag::schedule[]
    public void schedule() {
        akka.system().scheduler().schedule(
                //Initial delay 0 milliseconds
                Duration.create(0, TimeUnit.MILLISECONDS),
                //Frequency 30 minutes
                Duration.create(30, TimeUnit.MINUTES),
                receiver,
                "tick",
                akka.system().dispatcher(),
                null
        );
    }
    // end::schedule[]

    // tag::runOnce[]
    public void runOnce() {
        akka.system().scheduler().scheduleOnce(
                Duration.create(10, TimeUnit.MILLISECONDS),
                new Runnable() {
                    public void run() {
                        // ...
                    }
                },
                akka.system().dispatcher()
        );
    }
    // end::runOnce[]
}

