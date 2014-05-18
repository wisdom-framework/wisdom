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
package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.DefaultValue;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.util.List;

@Controller
public class ParameterController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/parameter/integer/{i}")
    public Result takeInt(@Parameter("i") int i) {
        return ok(Integer.toString(i));
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/query/integer")
    public Result takeIntFromQuery(@Parameter("i") int i) {
        return ok(Integer.toString(i));
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/long/{l}")
    public Result takeLong(@Parameter("l") long l) {
        return ok(Long.toString(l));
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/query/long")
    public Result takeLongFromQuery(@Parameter("l") long l) {
        return ok(Long.toString(l));
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/string/{s}")
    public Result takeString(@Parameter("s") String s) {
        return ok(s);
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/query/string")
    public Result takeStringFromQuery(@Parameter("s") String s) {
        return ok(s);
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/boolean/{b}")
    public Result takeBoolean(@Parameter("b") boolean b) {
        return ok(Boolean.toString(b));
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/query/boolean")
    public Result takeBooleanFromQuery(@Parameter("b") boolean b) {
        return ok(Boolean.toString(b));
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/query/boolean/default")
    public Result takeBooleanFromQueryWithDefault(@Parameter("b") @DefaultValue("on") boolean b) {
        return ok(Boolean.toString(b));
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/query/array")
    public Result takeList(@Parameter("x") int[] xs) {
        StringBuilder acc = new StringBuilder();
        for (int x : xs) {
            acc.append(x);
        }
        return ok(acc.toString());
    }

    @Route(method = HttpMethod.GET, uri = "/parameter/query/list")
    public Result takeList(@Parameter("x") List<Integer> xs) {
        StringBuilder acc = new StringBuilder();
        for (int x : xs) {
            acc.append(x);
        }
        return ok(acc.toString());
    }


}
