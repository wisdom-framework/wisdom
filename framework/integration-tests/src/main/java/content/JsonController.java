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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.util.List;

@Controller
public class JsonController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/json/jsonWithContentTypeAndCharset")
    public Result jsonWithContentTypeAndCharset() {
        return ok("{}").as("application/json; charset=utf-8");
    }

    @Route(method = HttpMethod.GET, uri = "/json/jsonWithContentType")
    public Result jsonWithContentType() {
        if (request().accepts(MimeTypes.JSON)) {
            return ok("{\"accept\":\"" + request().getHeader(HeaderNames.ACCEPT) + "\"}").as("application/json");
        } else {
            return internalServerError("Unexpected accept type");
        }
    }

    @Route(method = HttpMethod.POST, uri = "/json/post1")
    public Result post(@Body JsonNode node) {
        return ok(node);
    }

    @Route(method = HttpMethod.POST, uri = "/json/post2")
    public Result post() {
        return ok(context().body(JsonNode.class));
    }

    @Route(method = HttpMethod.GET, uri = "/json/user")
    public Result user() {
        return ok(new User(1, "wisdom", ImmutableList.of("coffee", "whisky"))).json();
    }

    @Route(method = HttpMethod.GET, uri = "/json/simple")
    public Result simple() {
        return ok("wisdom").json();
    }

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
