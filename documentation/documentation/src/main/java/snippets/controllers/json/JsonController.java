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
package snippets.controllers.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
public class JsonController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/json")
    public Result produceFromObject() {
        Person p = new Person();
        p.name = "wisdom";
        p.age = 1;
        return ok(p).json();
    }

    @Route(method = HttpMethod.POST, uri = "/json")
    public Result consume() {
        System.out.println("Body: " + context().body());
        JsonNode content = context().body(JsonNode.class);
        return ok(content.toString());
    }

    // tag::hello-json[]
    @Route(method = HttpMethod.POST, uri = "/json/hello")
    public Result hello() {
        JsonNode json = context().body(JsonNode.class);
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            String name = json.findPath("name").textValue();
            if (name == null) {
                return badRequest("Missing parameter [name]");
            } else {
                return ok("Hello " + name);
            }
        }
    }
    // end::hello-json[]

    // tag::hello-json-with-body[]
    @Route(method = HttpMethod.POST, uri = "/json/hello2")
    public Result helloWithBody(@NotNull @Body JsonNode json) {
        String name = json.findPath("name").textValue();
        if (name == null) {
            return badRequest("Missing parameter [name]");
        } else {
            return ok("Hello " + name);
        }
    }
    // end::hello-json-with-body[]

    // tag::hello-json-with-body-and-bean[]
    @Route(method = HttpMethod.POST, uri = "/json/hello3")
    public Result helloWithBodyUsingBean(@Valid @Body Person person) {
        return ok("Hello " + person.name);
    }
    // end::hello-json-with-body-and-bean[]

    // tag::build-json-using-json-service[]
    @Requires Json json;                                                    // <1>

    @Route(method = HttpMethod.POST, uri = "/json/hello4")
    public Result helloReturningJsonNode(@Valid @Body Person person) {
        ObjectNode result = json.newObject();                               // <2>
        result.put("name", person.name);
        result.put("message", "hello " + person.name);
        return ok(result);                                                  // <3>
    }
    // end::build-json-using-json-service[]

    // tag::build-json-using-mapping[]
    private class Response {                                                // <1>
        public final String name;
        public final String message;

        private Response(String name) {
            this.name = name;
            this.message = "hello " + name;
        }
    }

    @Route(method = HttpMethod.POST, uri = "/json/hello5")
    public Result helloReturningJsonObject(@Valid @Body Person person) {
        Response response = new Response(person.name);
        return ok(response).json();                                         // <2>
    }
    // end::build-json-using-mapping[]

    // tag::custom-serialization[]
    @Route(method = HttpMethod.POST, uri = "/json/car")
    public Result getACar() {
        Car car = new Car("renault", "clio 2", 4, "dirty");
        return ok(car).json();
    }
    // end::custom-serialization[]

}
