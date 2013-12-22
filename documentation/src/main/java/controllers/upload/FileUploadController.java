package controllers.upload;

import org.apache.commons.io.IOUtils;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.io.IOException;
import java.util.Arrays;

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
    public Result upload(@Attribute("upload") FileItem uploaded) {
        return ok("File " + uploaded.name() + " of type " + uploaded.mimetype() +
                " uploaded (" + uploaded.size() + " bytes)");
    }
    // end::upload-from-form[]

    // tag::upload-from-ajax[]
    @Route(method = HttpMethod.POST, uri = "/ajax")
    public Result ajax() throws IOException {
        byte[] content = IOUtils.toByteArray(context().getReader());
        return ok(content.length + " bytes uploaded");
    }
    // end::upload-from-ajax[]

}
