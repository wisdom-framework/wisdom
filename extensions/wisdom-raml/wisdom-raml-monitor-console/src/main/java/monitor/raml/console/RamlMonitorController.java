/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package monitor.raml.console;

import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.asset.Asset;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Simple Wisdom Monitor extension that looks for .raml files into <code>/assets/raml</code> and add the
 * Raml API Console for each of them. 
 * </p>
 *
 * <p>The file name must match <code>[A-Za-z0-9_-]+\.raml$<code/></p>
 *
 * @version 1.0
 * @author barjo
 */
@Controller
@Path("/monitor/raml/{name}")
public class RamlMonitorController extends DefaultController {

    /**
     * The raml file extension.
     */
    private static final String RAML_EXT = ".raml";

    /**
     * The assets directory that contains the raml files.
     */
    private static final String RAML_ASSET_DIR = "/assets/raml/";

    private BundleContext context;

    private Set<ServiceRegistration> registrations = new LinkedHashSet<>();

    /**
     * Names of the published raml spec.
     */
    private Set<String> names = new LinkedHashSet<>();

    /**
     * Template used to render the raml spec.
     */
    @View("monitor/ramlconsole")
    Template template;

    /**
     * The Assets service published by wisdom.
     */
    @Requires
    private Assets assets;

    /**
     * Return the raml console api corresponding to the raml of given name.
     *
     * @response.mime text/html
     * @param name Name of the raml api to display.
     * @return the raml console api or 404 if the file of given name doesn't exist in wisdom
     */
    @Route(method = HttpMethod.GET, uri = "")
    public Result index(@PathParameter("name") String name) {
        if(names.contains(name)){
            return ok(render(template,"source",RAML_ASSET_DIR+name+RAML_EXT));
        }
        return notFound();
    }

    /**
     * Retrieve the BundleContext from this constructor.
     *
     * @param context The bundle context used in order to register the RamlMonitorConsole services.
     */
    public RamlMonitorController(BundleContext context) {
        this.context = context;
    }

    /**
     * Looks for the raml file available in the assets and publish a RamlMonitorConsole for each of them.
     */
    @Validate
    public void start(){
        //Publish the monitor extension
        for(Asset asset : assets.assets()) {
            if (asset.getPath().matches("^"+RAML_ASSET_DIR+"[A-Za-z0-9_-]+\\"+RAML_EXT+"$")){
                String name = asset.getPath().substring(RAML_ASSET_DIR.length(), asset.getPath().length() - RAML_EXT.length());

                if(!names.add(name)){ //skip if two Controller have the same name, it's impossible
                    continue;
                }
                registrations.add(context.registerService(MonitorExtension.class, new RamlMonitorConsole(name), null));
            }
        }
    }

    /**
     * Unregister all RamlMonitorConsole services created by this instance.
     */
    @Invalidate
    public void stop(){
        for(ServiceRegistration registration: registrations){
            registration.unregister();
        }
        registrations.clear();
        names.clear();
    }
}
