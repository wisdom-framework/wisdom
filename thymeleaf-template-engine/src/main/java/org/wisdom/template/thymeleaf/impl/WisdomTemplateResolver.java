package org.wisdom.template.thymeleaf.impl;

import org.wisdom.template.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Specific resolver returning the template files from the template directory.
 */
public class WisdomTemplateResolver extends TemplateResolver {

    public WisdomTemplateResolver(TemplateEngine engine) {
        super();
        super.setResourceResolver(new WisdomURLResourceResolver(engine));
    }


}
