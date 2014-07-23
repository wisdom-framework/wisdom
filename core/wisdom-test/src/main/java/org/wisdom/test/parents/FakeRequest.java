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
package org.wisdom.test.parents;

import com.google.common.net.MediaType;
import org.wisdom.api.cookies.Cookies;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Request;

import java.util.*;

/**
 * A fake implementation of request.
 */
public class FakeRequest extends Request {
    private final FakeContext context;
    private final Map<String, Object> data = new HashMap<>();

    public FakeRequest(FakeContext fakeContext) {
        this.context = fakeContext;
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
    @Override
    public String contentType() {
        return context.header(HeaderNames.CONTENT_TYPE);
    }

    /**
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return {@literal null} as it's a fake request.
     */
    @Override
    public String method() {
        return null;
    }

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p/>
     * When the parameter is multivalued, returns the first value.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The value, or null if no parameter was found.
     * @see #parameterMultipleValues
     */
    @Override
    public String parameter(String name) {
        return context.parameter(name);
    }

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The values, possibly an empty list.
     */
    @Override
    public List<String> parameterMultipleValues(String name) {
        return context.parameterMultipleValues(name);
    }

    /**
     * Same like {@link #parameter(String)}, but returns given defaultValue
     * instead of null in case parameter cannot be found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public String parameter(String name, String defaultValue) {
        return context.parameter(name, defaultValue);
    }

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Integer if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or null if not found.
     */
    @Override
    public Integer parameterAsInteger(String name) {
        return context.parameterAsInteger(name);
    }

    /**
     * Same like {@link #parameter(String, String)}, but converts the
     * parameter to Integer if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public Integer parameterAsInteger(String name, Integer defaultValue) {
        return context.parameterAsInteger(name, defaultValue);
    }

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or {@literal false} if not found.
     */
    @Override
    public Boolean parameterAsBoolean(String name) {
        return context.parameterAsBoolean(name);
    }

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter or the defaultValue if not found.
     */
    @Override
    public Boolean parameterAsBoolean(String name, boolean defaultValue) {
        return context.parameterAsBoolean(name, defaultValue);
    }

    /**
     * Get all the parameters from the request.
     *
     * @return The parameters
     */
    @Override
    public Map<String, List<String>> parameters() {
        return context.parameters();
    }

    /**
     * Retrieves the data shared by all the entities participating to the request resolution (i.e. computation of the
     * response). This method returns a live map, meaning that modification impacts all other participants. It can be
     * used to let filters or interceptors passing objects to action methods or templates.
     *
     * @return the map storing the data. Unlike session or flash, these data are not stored in cookies,
     * and are cleared once the response is sent back to the client.
     */
    @Override
    public Map<String, Object> data() {
        return data;
    }

    /**
     * @return {@literal null} as this is a fake request.
     */
    @Override
    public String uri() {
        return null;
    }

    /**
     * The client IP address.
     *
     * @return {@literal null} as this is a fake request.
     */
    @Override
    public String remoteAddress() {
        return null;
    }

    /**
     * @return {@literal null} as this is a fake request.
     */
    @Override
    public String host() {
        return null;
    }

    /**
     * @return The URI path (without the query).
     */
    @Override
    public String path() {
        return context.path();
    }

    /**
     * @return {@literal null} as this is a fake request.
     */
    @Override
    public MediaType mediaType() {
        return null;
    }

    /**
     * @return {@literal empty} as this is a fake request.
     */
    @Override
    public Collection<MediaType> mediaTypes() {
        return Collections.emptyList();
    }

    /**
     * Checks if this request accepts a given media type.
     *
     * @param mimeType the mime type to check.
     * @return {@literal null} as this is a fake request.
     */
    @Override
    public boolean accepts(String mimeType) {
        return false;
    }

    /**
     * @return the request cookies
     */
    @Override
    public Cookies cookies() {
        return context.cookies();
    }

    /**
     * Retrieves all headers.
     *
     * @return headers
     */
    @Override
    public Map<String, List<String>> headers() {
        return context.headers();
    }

    /**
     * Get the encoding that is acceptable for the client. E.g. Accept-Encoding:
     * compress, gzip
     * <p/>
     * The Accept-Encoding request-header field is similar to Accept, but
     * restricts the content-codings that are acceptable in the response.
     *
     * @return the encoding that is acceptable for the client
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String encoding() {
        return context.header(HeaderNames.ACCEPT_ENCODING);
    }

    /**
     * Get the language that is acceptable for the client. E.g. Accept-Language:
     * da, en-gb;q=0.8, en;q=0.7
     * <p/>
     * The Accept-Language request-header field is similar to Accept, but
     * restricts the set of natural languages that are preferred as a response
     * to the request.
     *
     * @return the language that is acceptable for the client
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String language() {
        return context.header(HeaderNames.ACCEPT_LANGUAGE);
    }

    /**
     * Get the charset that is acceptable for the client. E.g. Accept-Charset:
     * iso-8859-5, unicode-1-1;q=0.8
     * <p/>
     * The Accept-Charset request-header field can be used to indicate what
     * character sets are acceptable for the response. This field allows clients
     * capable of understanding more comprehensive or special- purpose character
     * sets to signal that capability to a server which is capable of
     * representing documents in those character sets.
     *
     * @return the charset that is acceptable for the client
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String charset() {
        return context.header(HeaderNames.ACCEPT_CHARSET);
    }
}
