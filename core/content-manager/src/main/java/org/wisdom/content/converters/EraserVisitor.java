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

import java.lang.reflect.*;

/**
 * A visitor to compute the erased type.
 */
public final class EraserVisitor {

    public static final EraserVisitor ERASER = new EraserVisitor();

    private EraserVisitor() {
        // Singleton.
    }

    public final Class visit(Type type) {
        if (type instanceof Class) {
            return onClass((Class) type);
        }
        if (type instanceof ParameterizedType) {
            return onParameterizedType((ParameterizedType) type);
        }
        if (type instanceof GenericArrayType) {
            return onGenericArray((GenericArrayType) type);
        }
        if (type instanceof WildcardType) {
            return onWildcard((WildcardType) type);
        }
        if (type instanceof TypeVariable) {
            return onVariable((TypeVariable) type);
        }
        // We cover all cases
        throw new RuntimeException("Unexpected type " + type);
    }

    private Class onClass(Class clazz) {
        return clazz;
    }

    private Class onParameterizedType(ParameterizedType type) {
        return visit(type.getRawType());
    }

    private Class onGenericArray(GenericArrayType type) {
        return Array.newInstance(visit(type.getGenericComponentType()), 0).getClass();
    }

    private Class onVariable(TypeVariable type) {
        return visit(type.getBounds()[0]);
    }

    private Class onWildcard(WildcardType type) {
        return visit(type.getUpperBounds()[0]);
    }
}
