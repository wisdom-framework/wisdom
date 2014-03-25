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
package org.wisdom.samples.wamp;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
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

    @ServiceProperty(name = EventConstants.EVENT_TOPIC)
    private String[] topics = new String[]{"simple"};

    private ExportedService ref;

    private final static Logger LOGGER = LoggerFactory.getLogger(SampleWampController.class);

    @Validate
    public void start() throws RegistryException {
        LOGGER.debug("Published service: " + wamp.getServices());
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
        LOGGER.info("Receiving message from {} with {}", event.getTopic(), event.getProperty(Wamp.WAMP_EVENT_PROPERTY));
    }
}
