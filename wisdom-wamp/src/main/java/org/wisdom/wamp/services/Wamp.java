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
package org.wisdom.wamp.services;

import java.util.Collection;
import java.util.Map;

/**
 * Service allowing components to register services accessible using WAMP.
 * All public methods from the registered objects are accessible.
 * <p/>
 * The Wamp implementation must support the Event Admin bridge. All Wamp events received from Wamp clients are
 * transferred on the Event Admin using the following mapping:
 * <ul>
 * <li>the topic are computed using {@link org.wisdom.wamp.services.Wamp#getEventAdminTopicFromWampTopic(String)}</li>
 * <li>the event payload is passed as {@link com.fasterxml.jackson.databind.JsonNode} in the
 * {@link org.wisdom.wamp.services.Wamp#WAMP_EVENT_PROPERTY} property</li>
 * <li>the {@link org.wisdom.wamp.services.Wamp#WAMP_TOPIC_EVENT_PROPERTY} property of the Event Admin event
 * contains the original Wamp topic</li>
 * </ul>
 * <p/>
 * The bridge is bidirectional. Event from the OSGi event admin sent on <pre>wamp/*</pre> topics are transferred to
 * Wamp using the following mapping:
 * <ul>
 * <li>the Wamp topic is computed using {@link org.wisdom.wamp.services.Wamp#getWampTopicFromEventAdminTopic(String)}</li>
 * <li>the transferred Wamp event in a Json object containing all properties of the event</li>
 * <li>the property {@link org.wisdom.wamp.services.Wamp#WAMP_EXCLUSIONS_EVENT_PROPERTY} and
 * {@link org.wisdom.wamp.services.Wamp#WAMP_ELIGIBLE_EVENT_PROPERTY} can be used to configure the list of
 * excluded or eligible clients</li>
 * </ul>
 */
public interface Wamp {

    /**
     * Property specifying the Wamp Exclusion list in OSGi Event Admin Event.
     * Notice that the property value is a list of String. Each String is a Wamp Client session.
     */
    public static final String WAMP_EXCLUSIONS_EVENT_PROPERTY = "wamp.exclusions";

    /**
     * Property specifying the Wamp Eligible list in OSGi Event Admin Event.
     * Notice that the property value is a list of String. Each String is a Wamp Client session.
     */
    public static final String WAMP_ELIGIBLE_EVENT_PROPERTY = "wamp.eligible";

    /**
     * Property containing the original Wamp topic for event transferred from Wamp to the OSGi Event Admin.
     */
    public static final String WAMP_TOPIC_EVENT_PROPERTY = "wamp.topic";

    /**
     * Property containing the Wamp event payload in the Event Admin Event. The type of the property is a
     * {@link com.fasterxml.jackson.databind.JsonNode}.
     */
    public static final String WAMP_EVENT_PROPERTY = "wamp.event";

    /**
     * Registers a service.
     *
     * @param service the service object
     * @param url     the url to access the service
     * @return the exported service
     * @throws RegistryException if the registration fails
     */
    ExportedService register(Object service, String url) throws RegistryException;

    /**
     * Registers a service.
     *
     * @param service    the service object
     * @param properties service properties
     * @param url        the url to access the service
     * @return the exported service
     * @throws RegistryException if the registration fails
     */
    ExportedService register(Object service, Map<String, Object> properties, String url) throws RegistryException;

    /**
     * Withdraws a service exported using WAMP.
     *
     * @param url the url of the service to withdraw
     */
    void unregister(String url);

    /**
     * Withdraws a service exported using WAMP.
     *
     * @param svc the exported service.
     */
    void unregister(ExportedService svc);

    /**
     * The set of exported services currently accessible using WAMP.
     *
     * @return the list of exported service (immutable and copied when this method is invoked).
     */
    Collection<ExportedService> getServices();

    /**
     * Gets the WAMP base url.
     *
     * @return the WAMP base url.
     */
    String getWampBaseUrl();

    /**
     * Transforms the given Event Admin topic to the WAMP topic.
     * Notice that topic should start with 'wamp/', if not 'wamp/' is added.
     * For example:
     * wamp/org/example is mapped to http://host:port/wamp/org/example
     * org/example is mapped to http://host:port/wamp/org/example
     *
     * @param topic the topic from the event admin
     * @return the associated WAMP topic
     */
    String getWampTopicFromEventAdminTopic(String topic);

    /**
     * Transforms a topic from WAMP to an Event Admin topic.
     * For example, the topic http://host:port/wamp/org/example is mapped to org/example.
     *
     * @param topic the topic from WAMP
     * @return the associated Event Admin topic
     */
    String getEventAdminTopicFromWampTopic(String topic);
}
