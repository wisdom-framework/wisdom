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
package org.wisdom.api.content;


/**
 * This interface is exposed by the Parameter Converter Engine implementation as a service.
 * It allows retrieving the converter to create object from a specific type from a String representation.
 * <p>
 * Implementations aggregates the available {@link org.wisdom.api.content.ParameterConverter} and chooses the 'right'
 * one.
 * <p>
 * This interface was renamed to {@link org.wisdom.api.content.ParameterFactories}. You should switch to this new one
 * . This interface is going to be removed in the near future.
 */
@Deprecated
public interface ParameterConverters extends ParameterFactories {
     // All members have been moved to ParameterFactories.
}
