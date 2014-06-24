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
package snippets.controllers.jsonp;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.util.List;

@Controller
@Path("/jsonp")
public class JsonPController extends DefaultController {
    // tag::renderable[]
    @Requires
    Json json;

    @Route(method = HttpMethod.GET, uri = "/render")
    public Result usingRender(@Parameter("callback") String callback) {
        JsonNode node = json.parse("{ \"foo\": \"bar\" }");
        return ok(callback, node);
    }
    // end::renderable[]

    // tag::json[]
    @Route(method = HttpMethod.GET, uri = "/json")
    public Result usingJsonService(@Parameter("callback") String callback) {
        return ok(json.toJsonP(callback, json.newObject().put("foo", "bar"))).as(MimeTypes.JAVASCRIPT);
    }

    @Route(method = HttpMethod.GET, uri = "/user")
    public Result user(@Parameter("callback") String callback) {
        return ok(json.toJsonP(callback, new User(1, "wisdom", ImmutableList.of("coffee",
                "whisky")))).as(MimeTypes.JAVASCRIPT);
    }
    // end::json[]

    private static class User {
        int id;
        String name;
        List<String> favorites;

        public User(int id, String name, ImmutableList<String> fav) {
            this.id = id;
            this.name = name;
            this.favorites = fav;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getFavorites() {
            return favorites;
        }

        public void setFavorites(List<String> favorites) {
            this.favorites = favorites;
        }
    }
}
