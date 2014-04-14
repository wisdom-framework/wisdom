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
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.List;


@Controller
@Path("/monitor/osgi/bundle")
public class BundleMonitorExtension extends DefaultController implements MonitorExtension {

    @View("bundles")
    Template bundles;

    @Context
    BundleContext context;

    /**
     * Just a simple bundle event counter.
     */
    private BundleEventCounter counter = new BundleEventCounter();

    @Validate
    public void start() {
        counter.start();
    }

    @Invalidate
    public void stop() {
        counter.stop();
    }

    @Route(method = HttpMethod.GET, uri = "")
    public Result bundle() {
        return ok(render(bundles));
    }

    @Route(method = HttpMethod.GET, uri = ".json")
    public Result bundles() {
        final List<BundleModel> list = BundleModel.bundles(context);
        return ok(ImmutableMap.of(
                "bundles", list,
                "events", counter.get(),
                "active", Integer.toString(getActiveBundleCount(list)),
                "installed", Integer.toString(getInstalledBundleCount(list)))).json();
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

    @Route(method = HttpMethod.GET, uri = "/{id}")
    public Result toggleBundle(@Parameter("id") long id) {
        Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            if (bundle.getState() == Bundle.ACTIVE) {
                try {
                    bundle.stop();
                } catch (BundleException e) {
                    return badRequest(e);
                }
            } else if (bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED) {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    return badRequest(e);
                }
            }
        }
        return ok();
    }

    @Route(method = HttpMethod.POST, uri = "/{id}")
    public Result updateBundle(@Parameter("id") long id) {
        Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            try {
                bundle.update();
            } catch (BundleException e) {
                return badRequest(e);
            }
        }
        return ok();
    }

    @Route(method = HttpMethod.DELETE, uri = "/{id}")
    public Result uninstallBundle(@Parameter("id") long id) {
        Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                return badRequest(e);
            }
        }
        return ok();
    }

    @Override
    public String label() {
        return "Bundles";
    }

    @Override
    public String url() {
        return "/monitor/osgi/bundle";
    }

    @Override
    public String category() {
        return "osgi";
    }

    /**
     * A simple class counting events.
     * No synchronization involved, as we can be off without be in troubles.
     */
    private class BundleEventCounter implements BundleListener {

        int counter = 0;

        public void start() {
            context.addBundleListener(this);
        }

        public void reset() {
            counter = 0;
        }

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

        public int get() {
            return counter;
        }
    }
}
