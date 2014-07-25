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
package org.wisdom.content.converters;

import java.lang.reflect.Type;

/**
 * Structure representing a raw type and it's related type. This structure is useful when handling generics,
 * and the type may contain metadata about generics.
 * <p>
 * Be ware that as this structure has a reference on the class object, it must not be cached or stored,
 * to avoid class leaks.
 */
public final class ClassTypePair<T> {

    private final Type type;
    private final Class<T> rawClass;

    /**
     * Creates new type-class pair.
     *
     * @param clazz raw class representing the type.
     * @param type  type behind the class.
     */
    public ClassTypePair(Class<T> clazz, Type type) {
        this.type = type;
        this.rawClass = clazz;
    }

    /**
     * Creates new type-class pair for a non-generic type.
     *
     * @param clazz raw class representing the type.
     */
    public ClassTypePair(Class<T> clazz) {
        this.type = clazz;
        this.rawClass = clazz;
    }

    /**
     * @return the class.
     */
    public Class<T> rawClass() {
        return rawClass;
    }

    /**
     * @return the actual type behind the class.
     */
    public Type type() {
        return type;
    }

}
