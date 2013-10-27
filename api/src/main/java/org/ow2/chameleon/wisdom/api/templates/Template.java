package org.ow2.chameleon.wisdom.api.templates;

import org.ow2.chameleon.wisdom.api.http.Renderable;

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
     * Renders the template
     * @param variables the parameters
     * @return the rendered object.
     */
    Renderable render(Map<String, Object> variables);

    /**
     * Renders the template without explicit variables.
     * @return the rendered object.
     */
    Renderable render();

}
