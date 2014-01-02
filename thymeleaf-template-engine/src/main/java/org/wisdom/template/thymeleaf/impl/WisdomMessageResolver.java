package org.wisdom.template.thymeleaf.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;
import org.wisdom.api.http.Context;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An implementation of Thymeleaf Message Resolver based on the Wisdom Internationalization Service.
 * @see org.thymeleaf.messageresolver.IMessageResolver
 * @see org.wisdom.api.i18n.InternationalizationService
 */
@Component
@Provides
@Instantiate
public class WisdomMessageResolver extends AbstractMessageResolver implements IMessageResolver {

    @Requires(optional = true)
    InternationalizationService i18n;

    @Override
    public MessageResolution resolveMessage(Arguments arguments, String key, Object[] messageParameters) {
        Locale[] locales = getLocales();

        String message = i18n.get(locales, key, messageParameters);

        if (message == null) {
            return null;
        }

        return new MessageResolution(message);

    }

    private Locale[] getLocales() {
        Context ctx = Context.context.get();
        if (ctx == null) {
            return new Locale[]{Locale.getDefault()};
        } else {
            return ctx.request().languages();
        }
    }
}
