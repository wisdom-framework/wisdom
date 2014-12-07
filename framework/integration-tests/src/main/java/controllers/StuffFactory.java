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
package controllers;

import org.wisdom.api.annotations.Service;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.api.http.Context;

@Service
public class StuffFactory implements ParameterFactory<Stuff> {

    /**
     * Creates a new instance of {@code T} from the given HTTP Context.
     *
     * @param context the HTTP context
     * @return the instance of T
     * @throws IllegalArgumentException if the instance of T cannot be created from the given context.
     */
    @Override
    public Stuff newInstance(Context context) throws IllegalArgumentException {
        Stuff stuff = new Stuff();
        if (context.header("header").equals("hh")) {
            stuff.message = "OK";
        } else {
            stuff.message = "KO";
        }
        return stuff;
    }

    /**
     * Gets the type created by this factory.
     *
     * @return the class of T
     */
    @Override
    public Class<Stuff> getType() {
        return Stuff.class;
    }
}
