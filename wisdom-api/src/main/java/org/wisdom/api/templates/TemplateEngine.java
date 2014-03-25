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

import java.util.Collection;

/**
 * Service provided by template engines.
 */
public interface TemplateEngine {

    /**
     * Gets the current list of templates.
     *
     * @return the current list of template
     */
    Collection<Template> getTemplates();

    /**
     * The name of the template engine.
     * @return the name of the template engine
     */
    String name();

    /**
     * The extension of the files processed by the template engine.
     * @return the extension without the '.', such as '.thymeleaf.html'
     */
    String extension();

}
