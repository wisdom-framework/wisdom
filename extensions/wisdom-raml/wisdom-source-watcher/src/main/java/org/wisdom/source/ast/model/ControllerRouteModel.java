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
package org.wisdom.source.ast.model;

import org.wisdom.api.http.HttpMethod;
import org.wisdom.source.ast.visitor.Visitor;

import java.util.*;

/**
 * Model of a wisdom {@link org.wisdom.api.annotations.Route} method source.
 *
 * @author barjo
 * @param <T> the type of the parameter pass to the visitor.
 */
public class ControllerRouteModel<T> implements Comparable<ControllerRouteModel>, Model<T> {

    private String description;

    private String methodName;

    private String path;

    private Set<String> bodySamples = Collections.emptySet();

    private List<String> responseCodes = Collections.emptyList();

    private List<String> responseDescriptions = Collections.emptyList();

    private List<String> responseBodies = Collections.emptyList();

    private Set<String> bodyMimes = Collections.emptySet();

    private Set<String> responseMimes = Collections.emptySet();

    private HttpMethod httpMethod;

    private Set<RouteParamModel<T>> params = new LinkedHashSet<>();

    /**
     * Get a textual description of this route.
     *
     * @return The description of this route. The javadoc content of the route method, by default.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of this route.
     *
     * @param description This route description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get this route declared path. It is relative to the controller path.
     *
     * @return The route relative path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set this route path.
     *
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get this route HttpMethod action.
     *
     * @return this route HttpMethod action.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Set this route httpMethod action.
     *
     * @param httpMethod The httpMethod action.
     */
    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Get the name of the method that implements this route.
     *
     * @return the name of the method that implements this route.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Set the name of the method that implement this route.
     *
     * @param methodName the method name.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Get the list of the parameter supported by this route.
     *
     * @return The list of parameter supported by this route.
     */
    public Set<RouteParamModel<T>> getParams() {
        return params;
    }

    /**
     * @return A sample of a body for this route.
     */
    public Set<String> getBodySamples() {
        return bodySamples;
    }

    /**
     * Give a list of exemples of body content that are accepted by this route.
     *
     * @param bodySamples The list of samples for this route accepted body content.
     */
    public void setBodySamples(Set<String> bodySamples) {
        this.bodySamples = bodySamples;
    }


    /**
     * @return Response codes for this route.
     */
    public List<String> getResponseCodes() {
        return responseCodes;
    }

    /**
     * Give a list of response code for this route.
     *
     * @param responseCodes The list of response code for this route.
     */
    public void setResponseCodes(List<String> responseCodes) {
        this.responseCodes = responseCodes;
    }

    /**
     * @return Response descriptions for this route.
     */
    public List<String> getResponseDescriptions() {
        return responseDescriptions;
    }

    /**
     * Give a list of response description for this route.
     *
     * @param responseDescriptions The list of response description for this route.
     */
    public void setResponseDescriptions(List<String> responseDescriptions) {
        this.responseDescriptions = responseDescriptions;
    }

    /**
     * @return Response bodies for this route.
     */
    public List<String> getResponseBodies() {
        return responseBodies;
    }

    /**
     * Give a list of response bodies for this route.
     *
     * @param responseBodies The list of response bodies for this route.
     */
    public void setResponseBodies(List<String> responseBodies) {
        this.responseBodies = responseBodies;
    }

    /**
     * Get the list of content-types accepted by this route.
     *
     * @return The list of the content-type accepted by this route.
     */
    public Set<String> getBodyMimes() {
        return bodyMimes;
    }

    /**
     * Set the list of content-type accepted by this route.
     *
     * @param bodyMimes the content-type accepted by this route.
     */
    public void setBodyMimes(Set<String> bodyMimes) {
        this.bodyMimes = bodyMimes;
    }

    /**
     * @return The list of the response content-type that this route can produce.
     */
    public Set<String> getResponseMimes() {
        return responseMimes;
    }

    public void setResponseMimes(Set<String> responseMimes) {
        this.responseMimes = responseMimes;
    }

    /**
     * Add a parameter to this route.
     *
     * @param routeParam the route parameter to add.
     */
    public void addParam(RouteParamModel routeParam) {
        params.add(routeParam);
    }

    /**
     * A bit dummy compareTo implementation use by the tree Map.
     *
     * @param rElem the {@link ControllerRouteModel} that we want to compare to <code>this</code>.
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(ControllerRouteModel rElem) {
        if (rElem == null) {
            throw new NullPointerException("Cannot compare to null");
        }

        if (rElem.equals(this)) {
            return 0;
        }

        int compare = getPath().compareTo(rElem.getPath());

        if(compare == 0){
            compare = getHttpMethod().compareTo(rElem.getHttpMethod());

            if(compare == 0){
                compare = getMethodName().compareTo(rElem.getMethodName());
            }
        }

        return compare;
    }

    /**
     * Convenient method that return <code>true</code> if both parameter are equals.
     * It supports null value.
     *
     * @param obj1 The first object to check for equality
     * @param obj2 The second object to check for equality
     * @return <code>true</code> if both object are areEquals or null, <code>false</code> otherwise.
     */
    private static boolean areEquals(Object obj1, Object obj2){
        return obj1 != null ? obj1.equals(obj2) : obj2 == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ControllerRouteModel<?> that = (ControllerRouteModel<?>) o;

        return areEquals(description, that.description) &&
                areEquals(methodName, that.methodName) &&
                areEquals(path, that.path) &&
                areEquals(bodySamples, that.bodySamples) &&
                areEquals(responseCodes,responseCodes) &&
                areEquals(responseDescriptions,responseDescriptions) &&
                areEquals(responseBodies,responseBodies) &&
                areEquals(bodyMimes, that.bodyMimes) &&
                areEquals(responseMimes,responseMimes) &&
                areEquals(httpMethod, that.httpMethod) &&
                areEquals(params,that.params);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + bodySamples.hashCode();
        result = 31 * result + responseCodes.hashCode();
        result = 31 * result + responseDescriptions.hashCode();
        result = 31 * result + responseBodies.hashCode();
        result = 31 * result + bodyMimes.hashCode();
        result = 31 * result + responseMimes.hashCode();
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        result = 31 * result + params.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Visitor visitor, T anything) {
        visitor.visit(this, anything);
    }

    @Override
    public String toString() {
        return "ControllerRouteModel{" +
                "description='" + description + '\'' +
                ", methodName='" + methodName + '\'' +
                ", path='" + path + '\'' +
                ", bodySamples=" + bodySamples +
                ", responseCodes=" + responseCodes +
                ", responseDescriptions=" + responseDescriptions +
                ", responseBodies=" + responseBodies +
                ", bodyMimes=" + bodyMimes +
                ", responseMimes=" + responseMimes +
                ", httpMethod=" + httpMethod +
                ", params=" + params +
                '}';
    }
}

