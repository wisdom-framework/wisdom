package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.QueryParameter;
import org.wisdom.api.http.*;
import org.apache.commons.fileupload.FileItem;

import static org.wisdom.api.http.HttpMethod.*;

/**
 * <p>
 * A controller that contains route with Parameter, PathParams and Body.
 * Related to issue #505.
 * </p>
 *
 * @see <a href="https://github.com/wisdom-framework/wisdom/issues/505">#505</a>
 */
@Controller
public class ParamAndBody extends DefaultController{

    @Route(method = GET, uri = "/param")
    public Result param(@Parameter("isAdmin") boolean isAdmin) {
        return ok();
    }

    @Route(method = GET, uri = "/pathparam")
    public Result pathParam(@PathParameter("fullInfos") boolean fullInfos) {
        return ok();
    }

    @Route(method = GET, uri = "/body")
    public Result body(@Body String user) {
        return ok();
    }

    @Route(method = GET, uri = "/mixed")
    public Result mixed(@Body String user, @FormParameter("fileItem") FileItem fileItem,
                        @QueryParameter("test") Integer test) {
        return ok();
    }
}
