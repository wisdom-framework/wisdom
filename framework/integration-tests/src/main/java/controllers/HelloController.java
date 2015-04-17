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

import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Requires;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.content.Json;
import org.wisdom.api.content.Xml;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Negotiation;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


@Controller
@Path("/hello")
public class HelloController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/plain")
    public Result asText() {
        return ok("Hello World");
    }

    @Route(method = HttpMethod.GET, uri = "/html")
    public Result asHtml() {
        return ok("<h1>Hello World</h1>").html();
    }

    @Route(method = HttpMethod.GET, uri = "/json")
    public Result asJson() {
        return ok("{\"message\":\"Hello World\"}").json();
    }

    @Route(method = HttpMethod.GET, uri = "/xml")
    public Result asXML() {
        return ok("<message>Hello World</message>").xml();
    }

    @Route(method = HttpMethod.GET, uri = "/json/mapping")
    public Result usingJsonMapping() {
        return ok(new Message("Hello World")).json();
    }

    @Route(method = HttpMethod.GET, uri = "/xml/mapping")
    public Result usingXMLMapping() {
        return ok(new Message("Hello World")).xml();
    }

    @Requires
    Json json;

    @Route(method = HttpMethod.GET, uri = "/json/node")
    public Result usingJsonNode() {
        return ok(json.newObject().put("message", "Hello World"));
    }

    @Requires
    Xml xml;

    @Route(method = HttpMethod.GET, uri = "/xml/node")
    public Result usingXMLDocument() {
        Document document = xml.newDocument();
        final Element message = document.createElement("message");
        message.setTextContent("Hello World");
        document.appendChild(message);
        return ok(document);
    }

    @Route(method = HttpMethod.GET, uri = "/accept")
    public Result accept() {
        if (request().accepts("application/json")) {
            return ok("json");
        } else if (request().accepts("text/html")) {
            return ok("html");
        } else {
            return badRequest();
        }
    }

    @Route(method = HttpMethod.GET, uri = "/negotiation/accept")
    public Result negotiation() {
        return Negotiation.accept(
                ImmutableMap.of(
                        MimeTypes.JSON, async(
                                new Callable<Result>() {
                                    @Override
                                    public Result call() throws Exception {
                                        return ok("{\"message\":\"hello\"}").json();
                                    }
                                }),
                        MimeTypes.HTML, async(
                                new Callable<Result>() {
                                    @Override
                                    public Result call() throws Exception {
                                        return ok("<h1>Hello</h1>").html();
                                    }
                                }
                        )));
    }

    @Route(method = HttpMethod.GET, uri = "/negotiation/accept/sync")
    public Result negotiationSync() {
        return Negotiation.accept(
                ImmutableMap.of(
                        MimeTypes.JSON, ok("{\"message\":\"hello\"}").json(),

                        MimeTypes.HTML, ok("<h1>Hello</h1>").html()));
    }

    @Route(method = HttpMethod.GET, uri = "/async/simple")
    public Result async() {
        return async(
                new Callable<Result>() {
                    @Override
                    public Result call() throws Exception {
                        return ok("x");
                    }
                }
        );
    }

    @Route(method = HttpMethod.GET, uri = "/async/annotation")
    @Async(timeout = 2)
    public Result asyncWithAnnotation() {
        return ok("x");
    }

    @Route(method = HttpMethod.GET, uri = "/async/complete_annotation")
    @Async(timeout = 2, unit = TimeUnit.SECONDS)
    public Result asyncWithCompleteAnnotation() {
        return ok("x");
    }


    @Route(method = HttpMethod.GET, uri = "/async/timeout")
    @Async(timeout = 1)
    public Result asyncTimeout() throws InterruptedException {
        Thread.sleep(10000);
        return ok("x");
    }

    @Route(method = HttpMethod.GET, uri = "/async/complete_timeout")
    @Async(timeout = 1, unit = TimeUnit.SECONDS)
    public Result asyncTimeoutWithCompleteAnnotation() throws InterruptedException {
        Thread.sleep(10000);
        return ok("x");
    }

    @Route(method = HttpMethod.GET, uri = "/redirect")
    public Result redirect() {
        return redirect("/hello/plain");
    }

    @Route(method = HttpMethod.POST, uri = "/form")
    public Result postForm(@Body FormObject formObject) {
        // formObject is parsed into the method and rendered as json
        return ok(formObject).json();
    }

    @View("routing/reverse")
    Template reverse;

    @Route(method = HttpMethod.GET, uri = "/reverse")
    public Result reverse() {
        return ok(render(reverse));
    }

    private static class Message {
        String message;

        public Message(String m) {
            this.message = m;
        }

        public void setMessage(String m) {
            message = m;
        }

        public String getMessage() {
            return message;
        }
    }

}
