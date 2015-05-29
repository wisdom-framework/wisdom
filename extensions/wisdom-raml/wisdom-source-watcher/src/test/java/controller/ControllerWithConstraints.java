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
package controller;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.QueryParameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.Result;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static org.wisdom.api.http.HttpMethod.GET;

@Controller
public class ControllerWithConstraints extends DefaultController {

    @Route(method = GET, uri = "/superman")
    public Result paramNotNull(@NotNull @QueryParameter("clark") String clark){
        return ok();
    }

    @Route(method = GET, uri = "/batman")
    public Result paramNotNullWithMessage(@NotNull(message = "batman...batman...batmannnn") @QueryParameter("bruce") String bruce){
        return ok();
    }

    @Route(method = GET, uri = "/spiderman")
    public Result paramMin(@Min(1962) @QueryParameter("peter") Integer peter){
        return ok();
    }

    @Route(method = GET, uri = "/chameleon")
    public Result paramMinWithMessage(@Min(message="Master of disguise",value = 1963) @QueryParameter("dmitri") Integer dmitri){
        return ok();
    }

    @Route(method = GET, uri = "/rahan")
    public Result paramMax(@Max(2010) @QueryParameter("son") Integer son){
        return ok();
    }

    @Route(method = GET, uri = "/crao")
    public Result paramMaxWithMessage(@Max(message="Homme qui marche debout",value = 2010) @QueryParameter("father") Integer father){
        return ok();
    }
}
