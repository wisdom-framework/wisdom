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
package snippets.controllers.i18n;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.i18n.InternationalizationService;
import org.wisdom.api.templates.Template;

import java.io.File;
import java.util.Locale;

@Controller
public class InternationalizedController extends DefaultController {

    // tag::retrieve-message[]
    @Requires
    private InternationalizationService i18n;

    @Route(method= HttpMethod.GET, uri = "internationalization/messages")
    public Result retrieveInternationalizedMessages() {
        return ok(
                "english: " + i18n.get(Locale.ENGLISH, "welcome") + "\n" +
                "french: " + i18n.get(Locale.FRENCH, "welcome") + "\n" +
                "default: " + i18n.get(InternationalizationService.DEFAULT_LOCALE, "welcome") + "\n"
        ).as(MimeTypes.TEXT);
    }
    // end::retrieve-message[]

    // tag::retrieve-message-request[]
    @Route(method= HttpMethod.GET, uri = "internationalization/request")
    public Result retrieveInternationalizedMessagesFromRequest() {
        return ok(
                i18n.get(request().languages(), "welcome")
        ).as(MimeTypes.TEXT);
    }
    // end::retrieve-message-request[]

    @Route(method= HttpMethod.GET, uri = "internationalization/directory")
    public Result format() {
        File dir = new File("conf");
        return ok(
                i18n.get(request().languages(), "files.summary", dir.list().length, dir.getName())
        ).as(MimeTypes.TEXT);
    }

    @View("upload/Internationalized")
    Template internationalized;

    @Route(method= HttpMethod.GET, uri = "internationalization")
    public Result index() {
        return ok(render(internationalized));
    }
}
