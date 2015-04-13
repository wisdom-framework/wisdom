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

import org.wisdom.source.ast.visitor.Visitor;

/**
 * @author barjo
 */
public class RouteParamModel<T> implements Model<T> {

    private String paramName;

    private String name;

    private String valueType;

    private String defaultValue;

    private ParamType paramType = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType type) {
        this.paramType = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public void accept(Visitor visitor, T anything) {
        visitor.visit(this,anything);
    }

    public enum ParamType{
        BODY,QUERY,PARAM,FORM,PATH_PARAM
    }
}
