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
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class SessionController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/session")
    public Result populate() {
        if (session().get("blah") == null) {
            session("blah", "42");
        } else {
            session().remove("blah");
        }
        session("foo", "bar");
        session("baz", "bah");
        return ok("Hello");
    }

    @Route(method = HttpMethod.GET, uri = "/session/clear")
    public Result clear() {
        session().clear();
        return ok("Hello");
    }


    @Route(method = HttpMethod.GET, uri = "/session/cookie")
    public Result useAnotherCookie() {
        session("foo", "bar");
        return ok("Hello").with(Cookie.cookie("toto", "titi").build());
    }

    @Route(method = HttpMethod.GET, uri = "/session/cookie/clear")
    public Result clearCookie() {
        return ok("Hello").without("toto");
    }
}
