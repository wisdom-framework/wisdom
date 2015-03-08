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
package org.wisdom.monitor.extensions.osgi;

import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.*;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Provides the OSGi bundle admin view.
 */
@Controller
@Path("/monitor/osgi/bundle")
@Authenticated("Monitor-Authenticator")
public class BundleMonitorExtension extends DefaultController implements MonitorExtension {

    /**
     * The template.
     */
    @View("monitor/bundles")
    Template bundles;

    /**
     * The bundle context.
     */
    @Context
    BundleContext context;

    /**
     * Just a simple bundle event counter.
     */
    private BundleEventCounter counter = new BundleEventCounter();

    /**
     * Starts the counter.
     */
    @Validate
    public void start() {
        counter.start();
    }

    /**
     * Stops the counter.
     */
    @Invalidate
    public void stop() {
        counter.stop();
    }

    /**
     * @return the HTML page.
     */
    @Route(method = HttpMethod.GET, uri = "")
    public Result bundle() {
        return ok(render(bundles));
    }

    /**
     * @return the list of bundles and event counts.
     */
    @Route(method = HttpMethod.GET, uri = ".json")
    public Result bundles() {
        final List<BundleModel> list = BundleModel.bundles(context);
        return ok(ImmutableMap.of(
                "bundles", list,
                "events", counter.get(),
                "active", getActiveBundleCount(list),
                "installed", getInstalledBundleCount(list))).json();
    }

    private int getInstalledBundleCount(List<BundleModel> bundles) {
        int count = 0;
        for (BundleModel bundle : bundles) {
            if (BundleStates.INSTALLED.equals(bundle.getState())) {
                count++;
            }
        }
        return count;
    }

    private int getActiveBundleCount(List<BundleModel> bundles) {
        int count = 0;
        for (BundleModel bundle : bundles) {
            if (BundleStates.ACTIVE.equals(bundle.getState())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Toggles the states of the bundle. If the bundle is active, the bundle is stopped. If the bundle is resolved or
     * installed, the bundle is started.
     *
     * @param id the bundle's id
     * @return OK if everything is fine, BAD_REQUEST if the bundle cannot be started or stopped correctly,
     * NOT_FOUND if there are no bundles with the given id.
     */
    @Route(method = HttpMethod.GET, uri = "/{id}")
    public Result toggleBundle(@Parameter("id") long id) {
        Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            if (! isFragment(bundle)) {
                if (bundle.getState() == Bundle.ACTIVE) {
                    try {
                        bundle.stop();
                    } catch (BundleException e) {
                        logger().error("Cannot stop bundle {}", bundle.getSymbolicName(), e);
                        return badRequest(e);
                    }
                } else if (bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED) {
                    try {
                        bundle.start();
                    } catch (BundleException e) {
                        logger().error("Cannot start bundle {}", bundle.getSymbolicName(), e);
                        return badRequest(e);
                    }
                }
            } else {
                if (bundle.getState() == Bundle.RESOLVED) {
                    try {
                        bundle.stop();
                    } catch (BundleException e) {
                        logger().error("Cannot stop bundle {}", bundle.getSymbolicName(), e);
                        return badRequest(e);
                    }
                }
            }
        }
        return ok();
    }

    /**
     * Updates the given bundle. The bundle is updated from the installation url.
     *
     * @param id the bundle's id
     * @return OK if the bundle is updated, BAD_REQUEST if an error occurs when the bundle is updated,
     * NOT_FOUND if there are no bundles with the given id.
     */
    @Route(method = HttpMethod.POST, uri = "/{id}")
    public Result updateBundle(@Parameter("id") long id) {
        final Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            return async(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    try {
                        logger().info("Updating bundle {} from {}", bundle.getSymbolicName(), bundle.getLocation());
                        bundle.update();
                        return ok();
                    } catch (BundleException e) {
                        logger().error("Cannot update bundle {}", bundle.getSymbolicName(), e);
                        return badRequest(e);
                    }
                }
            });
        }
    }

    /**
     * Installs a new bundle.
     *
     * @param bundle the bundle file
     * @param startIfNeeded  whether or not the bundle need to be started
     * @return the bundle page, with a flash message.
     */
    @Route(method = HttpMethod.POST, uri = "")
    public Result installBundle(@FormParameter("bundle") final FileItem bundle,
                                @FormParameter("start") @DefaultValue("false") final boolean startIfNeeded) {
        if (bundle != null) {
            return async(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    Bundle b;

                    try {
                        b = context.installBundle("file/temp/" + bundle.name(), bundle.stream());
                        logger().info("Bundle {} installed", b.getSymbolicName());
                    } catch (BundleException e) {
                        flash("error", "Cannot install bundle '" + bundle.name() + "' : "
                                + e.getMessage());
                        return bundle();
                    }

                    if (startIfNeeded  && ! isFragment(b)) {
                        try {
                            b.start();
                            flash("success", "Bundle '" + b.getSymbolicName() + "' installed and started");
                            return bundle();
                        } catch (BundleException e) {
                            flash("error", "Bundle '" + b.getSymbolicName() + "' installed but " +
                                    "failed to start: " + e.getMessage());
                            return bundle();
                        }
                    } else {
                        flash("success", "Bundle '" + b.getSymbolicName() + "' installed.");
                        return bundle();
                    }
                }
            });
        } else {
            logger().error("No bundle to install");
            flash("error", "Unable to install the bundle - no uploaded file");
            return bundle();
        }
    }

    /**
     * Uninstalls the given bundle.
     *
     * @param id the bundle's id
     * @return OK if the bundle is updated, BAD_REQUEST if an error occurs when the bundle is uninstalled,
     * NOT_FOUND if there are no bundles with the given id.
     */
    @Route(method = HttpMethod.DELETE, uri = "/{id}")
    public Result uninstallBundle(@Parameter("id") long id) {
        final Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            return async(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    try {
                        logger().info("Uninstalling bundle {}", bundle.getSymbolicName());
                        bundle.uninstall();
                        return ok();
                    } catch (BundleException e) {
                        logger().error("Cannot uninstall bundle {}", bundle.getSymbolicName(), e);
                        return badRequest(e);
                    }
                }
            });
        }
    }

    /**
     * @return "Bundles".
     */
    @Override
    public String label() {
        return "Bundles";
    }

    /**
     * @return the bundle page's url.
     */
    @Override
    public String url() {
        return "/monitor/osgi/bundle";
    }

    /**
     * @return "OSGi".
     */
    @Override
    public String category() {
        return "osgi";
    }

    /**
     * A simple class counting events.
     * No synchronization involved, as we can be off without be in troubles.
     */
    private class BundleEventCounter implements BundleListener {

        /**
         * Current count.
         */
        int counter = 0;

        /**
         * Starts counting.
         */
        public void start() {
            context.addBundleListener(this);
        }

        /**
         * Resets the counter.
         */
        public void reset() {
            counter = 0;
        }

        /**
         * Stops counting.
         */
        public void stop() {
            context.removeBundleListener(this);
        }

        /**
         * Receives notification that a bundle has had a lifecycle change.
         *
         * @param event The <code>BundleEvent</code>.
         */
        public void bundleChanged(BundleEvent event) {
            counter++;
        }

        /**
         * Gets the current counter value.
         *
         * @return the current counter value.
         */
        public int get() {
            return counter;
        }
    }

    /**
     * Checks whether or not the given bundle is a fragment
     * @param bundle the bundle
     * @return {@code true} if the bundle is a fragment, {@code false} otherwise.
     */
    public static boolean isFragment(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        return headers.get(Constants.FRAGMENT_HOST) != null;
    }
}
