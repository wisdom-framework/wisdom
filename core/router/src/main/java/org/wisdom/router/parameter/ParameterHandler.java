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
package org.wisdom.router.parameter;

import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.http.Context;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.api.router.parameters.Source;

/**
 * The handler managing the following annotations: {@link org.wisdom.api.annotations.QueryParameter},
 * {@link org.wisdom.api.annotations.PathParameter}, {@link org.wisdom.api.annotations.Parameter}.
 */
public class ParameterHandler implements RouteParameterHandler {

    @Override
    public Object create(ActionParameter argument, Context context, ParameterFactories engine) {
        final Source source = argument.getSource();

        if (source == Source.PARAMETER || source == Source.PATH) {
            // First try from path.
            String value = context.parameterFromPath(argument.getName());
            if (value != null) {
                return engine.convertValue(value, argument.getRawType(), argument.getGenericType(), argument.getDefaultValue());
            }
        }

        if (source == Source.PARAMETER || source == Source.QUERY) {
            // If not in path, check whether we can handle multiple-values.
            if (Bindings.supportMultipleValues(argument.getRawType())) {
                return engine.convertValues(context.parameterMultipleValues(argument.getName()), argument.getRawType(),
                        argument.getGenericType(), argument.getDefaultValue());
            } else {
                return engine.convertValue(context.parameter(argument.getName()), argument.getRawType(), argument.getGenericType(),
                        argument.getDefaultValue());
            }
        }

        // Not found try to build something from the default value if any.
        return engine.convertValue(null, argument.getRawType(), argument.getGenericType(),
                argument.getDefaultValue());

    }
}
