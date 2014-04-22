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
package org.wisdom.api.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InputStream;

/**
 * A service interface used to handle Json objects and String.
 * It is basically a layer on top of Jackson. Be aware that implementation should support the dynamic addition and
 * removal of serializer/deserializer, making this service the main entry point to the Json support.
 */
public interface Json {

    /**
     * Gets the current mapper.
     *
     * @return the mapper
     */
    public ObjectMapper mapper();

    /**
     * Maps the given object to a JsonNode.
     * In addition to the default Jackson transformation, serializer dynamically added to the Json support are used.
     *
     * @param data the data to transform to json
     * @return the resulting json node
     */
    public JsonNode toJson(final Object data);

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given Json object.
     *
     * @param json  the json node
     * @param clazz the class of the instance to construct
     * @return an instance of the class.
     */
    public <A> A fromJson(JsonNode json, Class<A> clazz);

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given Json string.
     *
     * @param json  the json string
     * @param clazz the class of the instance to construct
     * @return an instance of the class.
     */
    public <A> A fromJson(String json, Class<A> clazz);

    /**
     * Retrieves the string form of the given Json Object.
     *
     * @param json the json node, must not be {@literal null}
     * @return the String form of the object
     */
    public String stringify(JsonNode json);

    /**
     * Parses the given String to build a Json Node object.
     *
     * @param src the Json String, it must be a valid Json String, non null.
     * @return the resulting json node
     */
    public JsonNode parse(String src);

    /**
     * Reads the given Input Stream to build a Json Node object.
     *
     * @param stream the stream, it must be a valid Json String, non null. The stream is not closed by the
     *               implementations.
     * @return the resulting json node
     */
    public JsonNode parse(InputStream stream);

    /**
     * @return a new object node.
     */
    public ObjectNode newObject();

    /**
     * @return a new array node.
     */
    public ArrayNode newArray();

    /**
     * Gets the JSONP response for the given callback and value.
     * @param callback the callback name
     * @param data the data to transform to json
     * @return the String built as follows: "callback(json(data))"
     */
    public String toJsonP(final String callback, final Object data);

}
