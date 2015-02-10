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
package org.wisdom.samples.file;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A simple controller to upload file and retrieve them later.
 */
@Controller
public class FileController extends DefaultController {

    private File root;

    @View("files/index")
    private Template index;

    @Requires
    private Router router;

    public FileController(@Requires ApplicationConfiguration configuration) {
        root = new File(configuration.getBaseDir(), "uploads");
        root.mkdirs();
    }

    @Route(method = HttpMethod.GET, uri = "/file")
    public Result index() {
        return ok(render(index,
                ImmutableMap.<String, Object>of(
                        "files", toFileItems(root.listFiles()))
        )).html();
    }

    private List<UploadedFile> toFileItems(File[] files) {
        List<UploadedFile> items = Lists.newArrayList();
        if (files == null) {
            return items;
        }
        for (File file : files) {
            items.add(new UploadedFile(file));
        }
        return items;
    }

    @Route(method = HttpMethod.POST, uri = "/file")
    public Result upload(final @FormParameter("upload") FileItem file) throws
            IOException {
        if (file == null) {
            flash("error", "true");
            flash("message", "No uploaded file");
            return badRequest(index());
        }

        // This should be asynchronous.
        return async(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                File out = new File(root, file.name());
                if (out.exists()) {
                    out.delete();
                }
                FileUtils.moveFile(file.toFile(), out);
                flash("success", "true");
                flash("message", "File " + file.name() + " uploaded (" + out.length() + " bytes)");
                return index();
            }
        });
    }

    @Route(method = HttpMethod.GET, uri = "/file/{name}")
    public Result download(@Parameter("name") String name) {
        File file = new File(root, name);
        if (!file.isFile()) {
            flash("error", "true");
            flash("message", "The file " + file.getName() + " does not exist");
            return notFound(index());
        }
        return ok(file, true);
    }

}
