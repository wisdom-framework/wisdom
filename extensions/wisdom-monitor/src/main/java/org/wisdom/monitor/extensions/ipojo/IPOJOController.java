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
package org.wisdom.monitor.extensions.ipojo;

import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.extender.InstanceDeclaration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.Collection;
import java.util.List;

/**
 * The controller providing the monitoring capabilities for iPOJO.
 */
@Controller
@Path("/monitor/ipojo")
@Authenticated("Monitor-Authenticator")
public class IPOJOController extends DefaultController implements MonitorExtension {

    @View("monitor/ipojo")
    Template ipojo;

    @Context
    BundleContext context;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result ipojo() {
        return ok(render(ipojo));
    }


    @Route(method = HttpMethod.GET, uri = ".json")
    public Result data() {
        final List<InstanceModel> instances = InstanceModel.instances(context);
        final List<FactoryModel> factories = FactoryModel.factories(context);
        int valid = 0, invalid = 0, stopped = 0;
        for (InstanceModel model : instances) {
            if (model.getState().equals("VALID")) {
                valid++;
            } else if (model.getState().equals("INVALID")) {
                invalid++;
            } else if (model.getState().equals("STOPPED")) {
                stopped++;
            }
        }
        return ok(ImmutableMap.builder()
                .put("instances", instances)
                .put("factories", factories)
                .put("valid", valid)
                .put("invalid", invalid)
                .put("stopped", stopped)
                .put("unbound", Integer.toString(getUnboundDeclarationCount()))
                .build()).json();
    }

    private int getUnboundDeclarationCount() {
        int count = 0;
        try {
            Collection<ServiceReference<InstanceDeclaration>> list = context.getServiceReferences(InstanceDeclaration
                    .class, null);
            for (ServiceReference<InstanceDeclaration> ref : list) {
                InstanceDeclaration declaration = context.getService(ref);
                if (!declaration.getStatus().isBound()) {
                    count++;
                }
            }

        } catch (InvalidSyntaxException e) { //NOSONAR
            // Ignore it (filter null).
        }
        return count;
    }

    @Override
    public String label() {
        return "iPOJO";
    }

    @Override
    public String url() {
        return "/monitor/ipojo/";
    }

    @Override
    public String category() {
        return "osgi";
    }
}
