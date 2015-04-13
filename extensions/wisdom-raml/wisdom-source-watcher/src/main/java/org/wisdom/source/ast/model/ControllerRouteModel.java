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

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * @author barjo
 */
public class ControllerRouteModel<T> implements Comparable<ControllerRouteModel>,Model<T>{

    private String description;

    private String methodName;

    private String path;

    private List<String> bodySamples = EMPTY_LIST;

    private List<String> bodyMimes = EMPTY_LIST;

    private List<String> responseMimes = EMPTY_LIST;

    private HttpMethod httpMethod;

    private List<RouteParamModel<T>> params = new LinkedList<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<RouteParamModel<T>> getParams(){
        return params;
    }

    public List<String> getBodySamples() {
        return bodySamples;
    }

    public void setBodySamples(List<String> bodySamples) {
        this.bodySamples = bodySamples;
    }

    public List<String> getBodyMimes() {
        return bodyMimes;
    }

    public void setBodyMimes(List<String> bodyMimes) {
        this.bodyMimes = bodyMimes;
    }

    public List<String> getResponseMimes() {
        return responseMimes;
    }

    public void setResponseMimes(List<String> responseMimes) {
        this.responseMimes = responseMimes;
    }

    public void addParam(RouteParamModel routeParam) {
        params.add(routeParam);
    }

    /**
     * A bit dummy compareTo implementation use by the tree Map.
     * @param rElem the {@link ControllerRouteModel} that we want to compare to <code>this</code>.
     */
    @Override
    public int compareTo(ControllerRouteModel rElem) {
        if(rElem == null) {
            throw new NullPointerException("Cannot compare to null");
        }

        if(rElem.equals(this)){
            return 0;
        }

        int pathComp = getPath().compareTo(rElem.getPath());

        if(pathComp != 0){
            return pathComp;
        }

        int methodComp = getHttpMethod().compareTo(rElem.getHttpMethod());

        if(methodComp!=0){
            return methodComp;
        }

        return getMethodName().compareTo(rElem.getMethodName());
    }

    @Override
    public void accept(Visitor visitor, T anything) {
        visitor.visit(this,anything);
    }
}

