/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package controllers.generic;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * A controller receiving parameterized parameters.
 */
@Controller
public class ControllerReceivingGenerics extends DefaultController {

    @Route(method = HttpMethod.POST, uri = "/generic/1",
            accepts = "application/json")
    public Result getData1(@Body DataWrapper<Data1> data) {
        return ok(data.getNested()).json();
    }

    @Route(method = HttpMethod.POST, uri = "/generic/2",
            accepts = "application/json")
    public Result getData2(@Body DataWrapper<Data2> data) {
        return ok(data.getNested()).json();
    }

    @Route(method = HttpMethod.POST, uri = "/generic/1",
            accepts = "application/xml")
    public Result getData1AsXml(@Body DataWrapper<Data1> data) {
        return ok(data).xml();
    }

    @Route(method = HttpMethod.POST, uri = "/generic/2",
            accepts = "application/xml")
    public Result getData2AsXml(@Body DataWrapper<Data2> data) {
        return ok(data).xml();
    }

}
