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
