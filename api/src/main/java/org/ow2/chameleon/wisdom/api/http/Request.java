package org.ow2.chameleon.wisdom.api.http;

import org.ow2.chameleon.wisdom.api.bodies.RequestBody;

/**
 * An HTTP request.
 */
public abstract class Request extends RequestHeader {

    /**
     * The request body.
     */
    public abstract RequestBody body();

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
