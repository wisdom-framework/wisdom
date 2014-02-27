package org.wisdom.samples.wamp;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.samples.wamp.logic.Calc;
import org.wisdom.wamp.services.ExportedService;
import org.wisdom.wamp.services.RegistryException;
import org.wisdom.wamp.services.Wamp;

@Component
@Provides
@Instantiate
public class SampleWampController extends DefaultController implements EventHandler {

    @Requires
    private Wamp wamp;

    @Requires
    private EventAdmin ea;

    @ServiceProperty(name= EventConstants.EVENT_TOPIC)
    private String[] topics = new String[] {"simple"};

    private ExportedService ref;

    @Validate
    public void start() throws RegistryException {
        System.out.println("Published service: " + wamp.getServices());
        ref = wamp.register(new Calc(), "/calc");
    }

    @Invalidate
    public void stop() {
        if (ref != null) {
            wamp.unregister(ref);
        }
    }

    /**
     * Called by the {@link org.osgi.service.event.EventAdmin} service to notify the listener of an
     * event.
     *
     * @param event The event that occurred.
     */
    @Override
    public void handleEvent(Event event) {
        System.out.println("Receiving message from " + event.getTopic() + " with " + event.getProperty(Wamp.WAMP_EVENT_PROPERTY));
    }
}
