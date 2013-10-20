package org.ow2.chameleon.wisdom.api.http;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.net.MediaType;
import org.ow2.chameleon.wisdom.api.cookies.Cookie;
import org.ow2.chameleon.wisdom.api.cookies.Cookies;

public abstract class RequestHeader {
    /**
     * The complete request URI, containing both path and query string.
     */
    public abstract String uri();

    /**
     * The client IP address.
     *
     * If the <code>X-Forwarded-For</code> header is present, then this method will return the value in that header
     * if either the local address is 127.0.0.1, or if <code>trustxforwarded</code> is configured to be true in the
     * application configuration file.
     */
    public abstract String remoteAddress();

    /**
     * The request host.
     */
    public abstract String host();
    /**
     * The URI path.
     */
    public abstract String path();

    /**
     * @return The media types set in the request Accept header, sorted by preference (preferred first).
     */
    public abstract MediaType mediaType();

    /**
     * Check if this request accepts a given media type.
     * @return true if <code>mimeType</code> is in the Accept header, otherwise false
     */
    public abstract boolean accepts(String mimeType);

    /**
     * @return the request cookies
     */
    public abstract Cookies cookies();

    /**
     * @param name Name of the cookie to retrieve
     * @return the cookie, if found, otherwise null.
     */
    public Cookie cookie(String name) {
        return cookies().get(name);
    }

    /**
     * Retrieves all headers.
     *
     * @return headers
     */
    public abstract java.util.Map<String,List<String>> headers();

    /**
     * Retrieves a single header.
     */
    public String getHeader(String headerName) {
        List<String> headers = null;
        for(String h: headers().keySet()) {
            if(headerName.toLowerCase().equals(h.toLowerCase())) {
                headers = headers().get(h);
                break;
            }
        }
        if(headers == null || headers.size() == 0) {
            return null;
        }
        return headers.get(0);
    }

    /**
     * Get the encoding that is acceptable for the client. E.g. Accept-Encoding:
     * compress, gzip
     *
     * The Accept-Encoding request-header field is similar to Accept, but
     * restricts the content-codings that are acceptable in the response.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     *
     * @return the encoding that is acceptable for the client
     */
    public abstract String encoding();

    /**
     * Get the language that is acceptable for the client. E.g. Accept-Language:
     * da, en-gb;q=0.8, en;q=0.7
     *
     * The Accept-Language request-header field is similar to Accept, but
     * restricts the set of natural languages that are preferred as a response
     * to the request.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     *
     * @return the language that is acceptable for the client
     */
    public abstract String language();

    /**
     * Get the charset that is acceptable for the client. E.g. Accept-Charset:
     * iso-8859-5, unicode-1-1;q=0.8
     *
     * The Accept-Charset request-header field can be used to indicate what
     * character sets are acceptable for the response. This field allows clients
     * capable of understanding more comprehensive or special- purpose character
     * sets to signal that capability to a server which is capable of
     * representing documents in those character sets.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     *
     * @return the charset that is acceptable for the client
     */
    public abstract String charset();

}
