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

/**
 * An HTTP request.
 */
public abstract class Request extends RequestHeader {
    // -- username

    private String username = null;

    /**
     * The user name for this request, if defined.
     * This is usually set by annotating your Action with <code>@Authenticated</code>.
     */
    public String username() {
        return username;
    }

    /**
     * Defines the user name for this request.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The Content-Type header field indicates the media type of the request
     * body sent to the recipient. E.g. {@code Content-Type: text/html;
     * charset=ISO-8859-4}
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     *
     * @return the content type of the incoming request.
     */
    public abstract String contentType();



    /**
     *
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return a <code>String</code>
     *        specifying the name
     *        of the method with which
     *        this request was made (eg GET, POST, PUT...)
     *
     */
    public abstract String method();

}
