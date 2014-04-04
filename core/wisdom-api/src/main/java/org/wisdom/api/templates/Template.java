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
package org.wisdom.api.templates;

import org.wisdom.api.Controller;
import org.wisdom.api.http.Renderable;

import java.util.Map;

/**
 * Template Service.
 * Templates are exposed as services.
 * Each template file is exposed as a service, and is accessible using a service dependency. To render the template,
 * use the render method.
 */
public interface Template {

    /**
     * @return the template name, usually the template file name without the extension.
     */
    String name();

    /**
     * @return the template full name. For example, for a file, it will be the file name (including extension).
     */
    String fullName();

    /**
     * @return the name of the template engine, generally the name of the technology.
     */
    String engine();

    /**
     * @return the mime type of the document produced by the template.
     */
    String mimetype();

    /**
     * Renders the template.
     * @param controller the controller having requested the rendering.
     * @param variables the parameters
     * @return the rendered object.
     */
    Renderable render(Controller controller, Map<String, Object> variables);

    /**
     * Renders the template without explicit variables.
     * @param controller the controller having requested the rendering.
     * @return the rendered object.
     */
    Renderable render(Controller controller);

}
