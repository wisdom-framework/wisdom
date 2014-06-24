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
package snippets.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.nio.charset.Charset;

@Controller
public class ManipulateResponse extends DefaultController {

    @Route(method= HttpMethod.GET, uri = "/manipulate")
    public Result index() {
        return ok().with(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    }

    // tag::text[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/text")
    public Result text() {
        return ok("Hello World!");
    }
    // end::text[]

    // tag::html[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/html")
    public Result htmlResult() {
        return ok("<h1>Hello World!</h1>").html();
    }
    // end::html[]

    // tag::html-2[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/html2")
    public Result htmlResult2() {
        return ok("<h1>Hello World!</h1>").as(MimeTypes.HTML);
    }
    // end::html-2[]

    // tag::json[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/json")
    public Result jsonResult() {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("hello", "world");
        return ok(node);
    }
    // end::json[]


    // tag::headers[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/headers")
    public Result headers() {
        return ok("<h1>Hello World!</h1>")
                .html()
                .with(CACHE_CONTROL, "max-age=3600")
                .with(ETAG, "xxx");
    }
    // end::headers[]


    // tag::cookies[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/cookies")
    public Result cookies() {
        return ok("<h1>Hello World!</h1>")
                .html()
                .with(Cookie.cookie("theme", "github").build());
    }
    // end::cookies[]

    // tag::remove-cookies[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/remove-cookies")
    public Result withoutCookies() {
        return ok("<h1>Hello World!</h1>")
                .html()
                .discard("theme");
    }
    // end::remove-cookies[]

    // tag::charset[]
    @Route(method = HttpMethod.GET, uri = "/manipulate/charset")
    public Result charset() {
        return ok("<h1>Hello World!</h1>")
                .html()
                .with(Charset.forName("iso-8859-1"));
    }
    // end::charset[]
}
