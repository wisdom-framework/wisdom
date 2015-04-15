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
package org.wisdom.source.ast.visitor;

import org.wisdom.source.ast.model.Model;

/**
 * Visitor of a {@link org.wisdom.source.ast.model.Model}.
 *
 * @author barjo
 * @param <M> The model specialisation
 * @param <V> The type of the object that is passed along the controllerParsed.
 */
public interface Visitor<M extends Model<V>,V> {

    /**
     * Visit the given model.
     *
     * @param model The model that we visit.
     * @param val the object that we pass along the visit.
     */
    void visit(M model, V val);
}
