package org.ow2.chameleon.wisdom.template.thymeleaf.impl;

import org.ow2.chameleon.wisdom.template.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.io.File;

/**
 * Specific resolver returning the template files from the template directory.
 */
public class WisdomResolver extends TemplateResolver {

    public WisdomResolver(TemplateEngine engine) {
        super();
        super.setResourceResolver(new WisdomURLResourceResolver(engine));
    }


}
