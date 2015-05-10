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

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.wisdom.source.ast.visitor.Visitor;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableMap;

/**
 * Model of a wisdom {@link org.wisdom.api.annotations.Controller} source.
 *
 * @param <T> the type of the parameter pass to the visitor.
 * @author barjo
 */
public class ControllerModel<T> implements Model<T> {

    /**
     * Default root path.
     */
    private static final String ROOT_PATH = "/";

    /**
     * The controller base path. It's annotated with {@link org.wisdom.api.annotations.Path}.
     */
    private String basePath = ""; //default value is empty

    /**
     * The controller name. The Class name by default.
     */
    private String name;

    /**
     * The controller description, it's the class javadoc.
     */
    private String description;

    /**
     * The controller version. Can be set via the javadoc <code>@version</code> tag.
     */
    private String version;

    /**
     * The controller routes, indexed by their path.
     */
    private TreeMultimap<String, ControllerRouteModel<T>> routes = TreeMultimap.create(
            new PathComparator(),
            new Comparator<ControllerRouteModel<T>>() {
                @Override
                public int compare(ControllerRouteModel<T> o1, ControllerRouteModel<T> o2) {
                    return o1.compareTo(o2);
                }
            }
    );

    /**
     * @return The controller routes, indexed by their path.
     */
    public NavigableMap<String, Collection<ControllerRouteModel<T>>> getRoutes() {
        return routes.asMap();
    }

    /**
     * @return The controller routes, indexed by their path.
     */
    public Multimap<String, ControllerRouteModel<T>> getRoutesAsMultiMap() {
        return routes;
    }

    /**
     * Accept to be visited by a visitor.
     *
     * @param visitor  A instance of a model visitor.
     * @param anything The object passed to the visitor.
     */
    @Override
    public void accept(Visitor visitor, T anything) {
        visitor.visit(this, anything);
    }

    /**
     * @return The controller name. Class name by default.
     */
    public String getName() {
        return name;
    }

    /**
     * Set a new name for the controller.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set a new path for the controller.
     *
     * @param basePath
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * @return The controller routes base path. It's annotated with {@link org.wisdom.api.annotations.Path}
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * @return The controller description, it's the class javadoc.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the controller description.
     *
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Adds the given {@link ControllerRouteModel} to <code>this.routes</code>.
     *
     * @param route
     */
    public void addRoute(ControllerRouteModel route) {
        routes.put(basePath + route.getPath(), route);
    }

    /**
     * @return The controller version. Set via the javadoc <code>@version</code> tag by default.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Change this controller model version.
     *
     * @param version This controller version
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
