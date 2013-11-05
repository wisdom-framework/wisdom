package org.ow2.chameleon.wisdom.samples.file;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Attribute;
import org.ow2.chameleon.wisdom.api.annotations.Parameter;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.http.FileItem;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A simple controller to upload file and retrieve them later.
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class FileController extends DefaultController {

    private File root;

    @Requires(filter="(name=files/index)")
    private Template index;

    @Requires
    private Router router;

    public FileController(@Requires ApplicationConfiguration configuration) {
        root = new File(configuration.getBaseDir(), "uploads");
        root.mkdirs();
    }

    @Route(method= HttpMethod.GET, uri = "/file")
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
    public Result upload(@Attribute("upload") FileItem file) throws IOException {
        if (file == null) {
            flash("error", "true");
            flash("message", "No uploaded file");
            return badRequest(index());
        }
        // This should be asynchronous.
        File out = new File(root, file.name());
        FileUtils.copyInputStreamToFile(file.stream(), out);
        flash("success", "true");
        flash("message", "File" + file.name() + " uploaded (" + out.length() + " bytes)");
        return index();
    }

    @Route(method = HttpMethod.GET, uri = "/file/{name}")
    public Result download(@Parameter("name") String name) {
        File file = new File(root, name);
        if (! file.isFile()) {
            flash("error", "true");
            flash("message", "The file " + file.getName() + " does not exist");
            return notFound(index());
        }
        return ok(file, true);
    }

}
