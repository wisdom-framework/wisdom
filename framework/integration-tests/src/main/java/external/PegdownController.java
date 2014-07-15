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
package external;

import org.apache.commons.io.IOUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.HttpParameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.io.IOException;
import java.io.Reader;

/**
 * A controller using Pegdown, a non-osgi library exported by the framework.
 * <p>
 * It reads markdown content from the request body and returns the output HTML.
 */
@Controller
public class PegdownController extends DefaultController {

    PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);

    @Route(method = HttpMethod.POST, uri = "/pegdown")
    public Result pegdown(@HttpParameter Reader markown) throws IOException {
        String md = IOUtils.toString(markown);
        return ok(processor.markdownToHtml(md)).html();
    }

}
