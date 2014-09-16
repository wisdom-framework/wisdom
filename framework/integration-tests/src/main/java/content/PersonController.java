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
package content;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
@Path("/person")
public class PersonController extends DefaultController {


    @Route(method = HttpMethod.POST, uri = "/json")
    public Result postPersonJson(@Body Person person) {
        return ok(person).json();
    }

    @Route(method = HttpMethod.POST, uri = "/xml")
    public Result postPersonXml(@Body Person person) {
        return ok(person).xml();
    }

    @Route(method = HttpMethod.POST, uri = "/accept")
    public Result postPerson(@Body Person person) {
        return ok(person);
    }
}
