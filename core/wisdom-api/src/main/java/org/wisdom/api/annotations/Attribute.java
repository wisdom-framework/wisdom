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
 * An annotation to inject a request attribute as argument. The type is inferred from the argument type.
 * This annotation can also be used to inject a {@link org.wisdom.api.http.FileItem} instance. To
 * achieve this the parameter type must be a File Item, and the specified value the field name set in the upload form.
 * <p/>
 * This annotation is retrieved and analyzed at runtime (by the router).
 *
 *
 * @deprecated use {@link org.wisdom.api.annotations.FormParameter} instead.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Attribute {

    /**
     * The attribute name.
     */
    String value();

}
