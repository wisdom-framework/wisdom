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
package cookies;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class FlashController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/flash")
    public Result populate() {
        flash("message", "Hello");
        flash().success("Success !");
        flash().error("Fail !");
        return ok("Hello");
    }

    @Route(method = HttpMethod.GET, uri = "/flash/no")
    public Result noflash() {
        // Retrieve the incoming flash
        return ok(flash("message") + " - " + flash("flash_success"));
    }
}
