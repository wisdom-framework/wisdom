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
package org.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation instructing Wisdom to build an object injected with other annotations.
 * This class of the annotated parameter must follows these rules:
 * <ol>
 * <li>Having a constructor without parameter or with parameter annotated with <em>parameter</em> annotations</li>
 * <li>Having setter methods starting with {@literal set} prefix and receiving a parameter annotated with one
 * of the <em>parameter</em> annotation.</li>
 * <li>Annotated parameters can use the {@link org.wisdom.api.annotations.DefaultValue} annotation to indicate
 * the injected value when the data is not available.</li>
 * <li>The {@link org.wisdom.api.annotations.BeanParameter} annotation can be used in combination of {@link
 * javax.validation.Valid} to ensure the validity ot the created object.</li>
 * </ol>
 * <p>
 * Parameter annotations are: {@link org.wisdom.api.annotations.HttpParameter}, {@link org.wisdom.api.annotations
 * .QueryParameter}, {@link org.wisdom.api.annotations.PathParameter}, {@link org.wisdom.api.annotations.Body} and
 * {@link org.wisdom.api.annotations.BeanParameter} (to build nested bean).
 * <p>
 * This annotation is retrieved and analyzed at runtime (by the router).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanParameter {

    //TODO Nested case.
}
