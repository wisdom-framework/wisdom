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
package snippets.controllers.upload;

import org.apache.commons.io.IOUtils;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.io.IOException;

/**
 * Example of File Upload
 */
@Controller
@Path("/uploads")
public class FileUploadController extends DefaultController {

    @View("upload/File-Upload")
    Template uploadPage;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result index() {
        return ok(render(uploadPage));
    }

    // tag::upload-from-form[]
    @Route(method = HttpMethod.POST, uri = "/")
    public Result upload(@FormParameter("upload") FileItem uploaded) {
        return ok("File " + uploaded.name() + " of type " + uploaded.mimetype() +
                " uploaded (" + uploaded.size() + " bytes)");
    }
    // end::upload-from-form[]

    // tag::upload-from-ajax[]
    @Route(method = HttpMethod.POST, uri = "/ajax")
    public Result ajax() throws IOException {
        byte[] content = IOUtils.toByteArray(context().reader());
        return ok(content.length + " bytes uploaded");
    }
    // end::upload-from-ajax[]

}
