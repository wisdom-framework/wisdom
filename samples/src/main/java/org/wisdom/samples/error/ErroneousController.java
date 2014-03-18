package org.wisdom.samples.error;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * Created by clement on 18/03/2014.
 */
@Controller
public class ErroneousController extends DefaultController {

    @Route(method= HttpMethod.GET, uri="/error")
    public Result doSomethingWrong() throws Exception {
        throw new Exception("Bad bad bad");
    }
}
