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
package controllers.errors;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.exceptions.HttpException;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.util.NoSuchElementException;

@Controller
@Path("/error")
public class ErroneousController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/500")
    public Result internal() {
        throw new NullPointerException("nope");
    }

    @Route(method = HttpMethod.GET, uri = "/418")
    public Result http() {
        throw new HttpException(418, "bad");
    }

    @Route(method = HttpMethod.GET, uri = "/300")
    public Result element() {
        throw new NoSuchElementException();
    }
}
