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
package org.wisdom.api.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * An HTTP request.
 */
public abstract class Request extends RequestHeader {

    private String username = null;

    /**
     * The user name for this request, if defined.
     * This is usually set by annotating your Action with <code>@Authenticated</code>.
     *
     * @return the username if any
     */
    public String username() {
        return username;
    }

    /**
     * Defines the user name for this request.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The Content-Type header field indicates the media type of the request
     * body sent to the recipient. E.g. {@code Content-Type: text/html;
     * charset=ISO-8859-4}
     *
     * @return the content type of the incoming request.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    public abstract String contentType();

    /**
     * Retrieves the mime-type part of the content-type header. For instance on {@code Content-Type: text/html;
     * charset=ISO-8859-4}, it retrieves {@code text/html}.
     *
     * @return the mime type of the content-type header
     */
    public String contentMimeType() {
        String ct = contentType();
        if (ct != null) {
            int index = ct.indexOf(";");
            if (index != -1) {
                return ct.substring(0, index);
            } else {
                return ct;
            }
        } else {
            return null;
        }
    }

    /**
     * Retrieves the charset part of the content-type header. For instance on {@code Content-Type: text/html;
     * charset=ISO-8859-4}, it retrieves {@code ISO-8859-4}.
     *
     * @return the charset of the content-type header, {@code null} if not set.
     */
    public Charset contentCharset() {
        String ct = contentType();
        if (ct != null) {
            int index = ct.indexOf("charset=");
            if (index != -1) {
                return Charset.forName(ct.substring(index + 8));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return a <code>String</code>
     * specifying the name
     * of the method with which
     * this request was made (eg GET, POST, PUT...)
     */
    public abstract String method();

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p>
     * When the parameter is multivalued, returns the first value.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The value, or null if no parameter was found.
     * @see #parameterMultipleValues
     */
    public abstract String parameter(String name);

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The values, possibly an empty list.
     */
    public abstract List<String> parameterMultipleValues(String name);

    /**
     * Same like {@link #parameter(String)}, but returns given defaultValue
     * instead of null in case parameter cannot be found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    public abstract String parameter(String name, String defaultValue);

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Integer if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or null if not found.
     */
    public abstract Integer parameterAsInteger(String name);

    /**
     * Same like {@link #parameter(String, String)}, but converts the
     * parameter to Integer if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    public abstract Integer parameterAsInteger(String name, Integer defaultValue);

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or {@literal false} if not found.
     */
    public abstract Boolean parameterAsBoolean(String name);

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter or the defaultValue if not found.
     */
    public abstract Boolean parameterAsBoolean(String name, boolean defaultValue);

    /**
     * Get all the parameters from the request.
     *
     * @return The parameters
     */
    public abstract Map<String, List<String>> parameters();

    /**
     * Retrieves the data shared by all the entities participating to the request resolution (i.e. computation of the
     * response). This method returns a live map, meaning that modification impacts all other participants. It can be
     * used to let filters or interceptors passing objects to action methods or templates.
     *
     * @return the map storing the data. Unlike session or flash, these data are not stored in cookies,
     * and are cleared once the response is sent back to the client.
     */
    public abstract Map<String, Object> data();

}
