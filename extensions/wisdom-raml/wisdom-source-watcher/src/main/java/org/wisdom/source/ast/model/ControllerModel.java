/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2015 Wisdom Framework
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
/*
 * Copyright 2015, Technologic Arts Vietnam.
 * All right reserved.
 */

package org.wisdom.source.ast.model;

import com.google.common.collect.TreeMultimap;
import org.wisdom.source.ast.visitor.Visitor;

/**
 * @author barjo
 */
public class ControllerModel<T> implements Model<T> {
    private static final String ROOT_PATH = "/";

    private String basePath;

    private String name;

    private String description;

    private String version;

    private TreeMultimap<String, ControllerRouteModel> routes = TreeMultimap.create();

    public TreeMultimap<String,ControllerRouteModel> getRoutes(){
        return routes;
    }

    @Override
    public void accept(Visitor visitor,T anything) {
        visitor.visit(this, anything);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * <p>
     * Add the give {@link ControllerRouteModel} to <code>this.routes</code>.
     * The full route path with a {@literal /} appended is added as a key. The {@literal /} is used for ordering, so that:
     * <br/>
     * <code>/toto/tata</code> is a child of <code>/toto</code>, but <code>/totoa</code> is not.
     * </p>
     * @param route
     */
    public void addRoute(ControllerRouteModel route) {
        if(route.getPath().equals(ROOT_PATH) || route.getPath().isEmpty()){
            routes.put(basePath+"/",route);
        } else{
            routes.put(basePath + route.getPath()+"/",route); //full path  + / for ordering
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version=version;
    }
}
