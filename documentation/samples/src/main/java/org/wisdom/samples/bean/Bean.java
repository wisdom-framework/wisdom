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
package org.wisdom.samples.bean;

import org.wisdom.api.annotations.DefaultValue;
import org.wisdom.api.annotations.QueryParameter;

import javax.validation.constraints.NotNull;

/**
 * A bean injected using the {@link org.wisdom.api.annotations.BeanParameter} annotation.
 */
public class Bean {


    private final String value;

    /**
     * You can add validation constraint on the injected value.
     */
    private @NotNull String anotherValue;

    /**
     * Values can be injected in the constructor, and combined with the {@link org.wisdom.api.annotations
     * .DefaultValue} annotation.
     *
     * @param v the value
     */
    public Bean(@DefaultValue("hello") @QueryParameter("q") String v) {
        this.value = v;
    }

    /**
     * A regular setter used to inject a second parameter.
     *
     * @param v the value
     */
    public void setAnotherValue(@QueryParameter("q2") String v) {
        this.anotherValue = v;
    }

    public String getValue() {
        return value;
    }

    public String getAnotherValue() {
        return anotherValue;
    }
}
