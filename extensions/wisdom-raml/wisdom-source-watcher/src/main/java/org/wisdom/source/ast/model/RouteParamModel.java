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
 * Model of a wisdom route parameter.
 *
 * @author barjo
 * @param <T> the type of the parameter pass to the visitor.
 */
public class RouteParamModel<T> implements Model<T> {

    private String paramName;

    private String name;

    private String valueType;

    private String defaultValue;

    private Boolean mandatory = false;

    private ParamType paramType = null;

    private Long min = null;

    private Long max = null;

    /**
     * Get the name of this parameter. (Given through the annotation, or like the {@link #getParamName()}).
     * @return The parameter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this parameter.
     * @param name the parameter name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The type of the parameter value (java type as String).
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * Set this parameter value type.
     * @param valueType The parameter value type as String. (Java type).
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    /**
     * @return this parameter type.
     */
    public ParamType getParamType() {
        return paramType;
    }

    /**
     * Set this parameter ParamType.
     * @param type The ParamType.
     */
    public void setParamType(ParamType type) {
        this.paramType = type;
    }

    /**
     * Get the default value of this parameter. As annotated by {@link org.wisdom.api.annotations.DefaultValue}.
     * @return the default value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value of this parameter.
     * @param defaultValue the default value.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Get this parameter name. (from its java name).
     * @return this parameter name
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * Set the name of this parameter.
     * @param paramName the parameter name.
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }


    /**
     * Check if the Parameter is mandatory, i.e has the NotNull constraint. This can be done thanks to the
     * {@link javax.validation.constraints.NotNull} annotation.
     *
     * @return true if the Parameter is annotated with NotNull.
     */
    public Boolean isMandatory(){
        return mandatory;
    }

    /**
     * Set if the Parameter can be mandatory or not.
     *
     * @param mandatory
     */
    public void setMandatory(Boolean mandatory){
        if(mandatory == null){
            throw new IllegalArgumentException("The mandatory parameter cannot be null");
        }

        this.mandatory = mandatory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Visitor visitor, T anything) {
        visitor.visit(this,anything);
    }

    /**
     * Set the minimum value of this parameter. This can be done thanks to the {@link javax.validation.constraints.Min}
     * annotation.
     *
     * @param min The minimum value.
     */
    public void setMin(Long min) {
        this.min = min;
    }

    /**
     * Get the Minimum value if it has been defined.
     *
     * @return The minimum value or null if not set.
     */
    public Long getMin() {
        return min;
    }

    /**
     * Set the maximum value of this parameter. This can be done thanks to the {@link javax.validation.constraints.Max}
     * annotation.
     *
     * @param max The maximum value.
     */
    public void setMax(Long max) {
        this.max = max;
    }

    /**
     * Get the maximum value if it has been defined.
     *
     * @return The maximum value or null if not set.
     */
    public Long getMax() {
        return max;
    }

    /**
     * The known parameter type.
     */
    public enum ParamType{
        BODY,QUERY,PARAM,FORM,PATH_PARAM
    }
}
