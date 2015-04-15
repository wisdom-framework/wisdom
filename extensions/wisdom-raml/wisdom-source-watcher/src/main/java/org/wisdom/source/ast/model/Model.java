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
 * A wisdom model that can be visited.
 *
 * @param <T> an object pass along the controllerParsed.
 */
public interface Model<T> {

    /**
     * Let the given visitor visit this model element.
     *
     * @param visitor The visitor that want to visit the model element
     * @param anything The parameter passed to the visitor during the visit
     */
    void accept(Visitor visitor,T anything);
}
