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
package org.wisdom.test.parents;

import org.apache.felix.ipojo.extender.queue.QueueService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.testing.helpers.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ow2.chameleon.testing.helpers.OSGiHelper.isFragment;

/**
 * Enhance the stability concepts from the OSGi Test Helpers with the a third check. With this class,
 * stability is reached when:
 * <ol>
 * <li>all (non-fragment) bundles are resolved, and no bundle events are fired in a defined time period.</li>
 * <li>all iPOJO processing queue are empty.</li>
 * <li>no service event are fired in a defined period.</li>
 * </ol>
 */
public class Stability {

    /**
     * The Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Stability.class);

    /**
     * The number of attempts.
     */
    public static final int MAX_TRIES = 500;


    /**
     * Waits for stability:
     * <ul>
     * <li>all bundles are activated
     * <li>service count is stable
     * </ul>
     * If the stability can't be reached after a specified time,
     * the method throws a {@link IllegalStateException}.
     *
     * @param context the bundle context
     * @throws IllegalStateException when the stability can't be reach after a several attempts.
     */
    public static void waitForStability(BundleContext context) throws IllegalStateException {
        waitForBundleStability(context);
        waitForIPOJOQueuesToBeEmpty(context);
        waitForServiceStability(context);
    }

    private static void waitForIPOJOQueuesToBeEmpty(BundleContext context) {
        int count = 0;
        try {
            Collection<ServiceReference<QueueService>> refs = context.getServiceReferences(QueueService.class, null);
            List<Object> queues = new ArrayList<>();
            for (ServiceReference<QueueService> ref : refs) {
                queues.add(context.getService(ref));
            }
            boolean emptyness = false;

            while (!emptyness && count < MAX_TRIES) {
                emptyness = areAllQueuesEmpty(queues);
                if (!emptyness) {
                    TimeUtils.grace(MAX_TRIES);
                }
                count++;
            }

        } catch (InvalidSyntaxException e) { //NOSONAR
            // Cannot happen, filter is null
        }

        if (count == MAX_TRIES) {
            LOGGER.error("iPOJO processing queues are not empty after 500 tries");
            throw new IllegalStateException("Cannot reach the service stability");
        }
    }

    /**
     * Checks whether or not all the iPOJO processing queue are empty or not.
     * Metrics are retrieved using reflection to avoid issues when iPOJO is in the classpath.
     *
     * @param queues the queues
     * @return {@literal true} if all the iPOJO processing queue are empty, {@literal false} otherwise.
     */
    private static boolean areAllQueuesEmpty(List<Object> queues) {
        boolean empty = true;

        for (Object q : queues) {
            try {
                Method currents = q.getClass().getMethod("getCurrents");
                Method waiters = q.getClass().getMethod("getWaiters");

                int cur = (int) currents.invoke(q);
                int wai = (int) waiters.invoke(q);

                LOGGER.debug("queue: " + q + " #current : " + cur + " / #waiting : " + wai);
                empty = empty && cur == 0 && wai == 0;
            } catch (Exception e) {
                LOGGER.error("Cannot analyze queue's metrics for {}", q, e);
                throw new IllegalArgumentException("Cannot analyze queue's metrics", e);
            }
        }
        return empty;
    }

    private static void waitForServiceStability(BundleContext context) {
        boolean serviceStability = false;
        int count = 0;
        int count1 = 0;
        int count2 = 0;
        while (!serviceStability && count < MAX_TRIES) {
            try {
                ServiceReference[] refs = context.getServiceReferences((String) null, null);
                count1 = refs.length;
                TimeUtils.grace(MAX_TRIES);
                refs = context.getServiceReferences((String) null, null);
                count2 = refs.length;
                serviceStability = count1 == count2;
            } catch (Exception e) {
                LOGGER.warn("An exception was thrown while checking the service stability", e);
                serviceStability = false;
                // Nothing to do, while recheck the condition
            }
            count++;
        }

        if (count == MAX_TRIES) {
            System.err.println("Service stability isn't reached after 500 tries (" + count1 + " != " + count2);
            throw new IllegalStateException("Cannot reach the service stability");
        }
    }

    public static void waitForBundleStability(BundleContext context) {
        boolean bundleStability = getBundleStability(context);
        int count = 0;
        while (!bundleStability && count < MAX_TRIES) {
            try {
                Thread.sleep(100 * TimeUtils.TIME_FACTOR);
            } catch (InterruptedException e) {
                // Interrupted
            }
            count++;
            bundleStability = getBundleStability(context);
        }

        if (count == MAX_TRIES) {
            LOGGER.error("Bundle stability isn't reached after 500 tries");
            for (Bundle bundle : context.getBundles()) {
                LOGGER.error("Bundle " + bundle.getBundleId() + " - " + bundle.getSymbolicName() + " -> " +
                        bundle.getState());
            }
            throw new IllegalStateException("Cannot reach the bundle stability");
        }
    }

    /**
     * Are bundle stables.
     *
     * @param bc the bundle context
     * @return <code>true</code> if every bundles are activated.
     */
    public static boolean getBundleStability(BundleContext bc) {
        boolean stability = true;
        Bundle[] bundles = bc.getBundles();
        for (Bundle bundle : bundles) {
            if (isFragment(bundle)) {
                stability = stability && (bundle.getState() == Bundle.RESOLVED);
            } else {
                stability = stability && (bundle.getState() == Bundle.ACTIVE);
            }
        }
        return stability;
    }
}
