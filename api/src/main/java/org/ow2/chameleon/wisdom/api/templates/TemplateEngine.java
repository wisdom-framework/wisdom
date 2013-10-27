package org.ow2.chameleon.wisdom.api.templates;

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

}
