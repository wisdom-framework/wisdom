package org.wisdom.engine.wrapper;

import com.google.common.net.MediaType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Request;
import org.wisdom.engine.wrapper.cookies.CookiesImpl;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Request implementation based on HttpRequest.
 */
public class RequestFromNetty extends Request {

    private final HttpRequest request;
    private final ChannelHandlerContext channel;
    private final Cookies cookies;

    public RequestFromNetty(ChannelHandlerContext ctx, HttpRequest request) {
        this.request = request;
        this.channel = ctx;
        this.cookies = new CookiesImpl(request);
    }

    /**
     * The Content-Type header field indicates the media type of the request
     * body sent to the recipient. E.g. {@code Content-Type: text/html;
     * charset=ISO-8859-4}
     *
     * @return the content type of the incoming request.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String contentType() {
        return request.headers().get(HeaderNames.CONTENT_TYPE);
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
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String encoding() {
        return request.headers().get(HeaderNames.ACCEPT_ENCODING);
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
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String language() {
        return request.headers().get(HeaderNames.ACCEPT_LANGUAGE);
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
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String charset() {
        return request.headers().get(HeaderNames.ACCEPT_CHARSET);
    }

    /**
     * The complete request URI, containing both path and query string.
     */
    @Override
    public String uri() {
        return request.getUri();
    }

    /**
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return a <code>String</code>
     *         specifying the name
     *         of the method with which
     *         this request was made (eg GET, POST, PUT...)
     */
    @Override
    public String method() {
        return request.getMethod().name();
    }

    /**
     * The client IP address.
     * <p/>
     * If the <code>X-Forwarded-For</code> header is present, then this method will return the value in that header
     * if either the local address is 127.0.0.1, or if <code>trustxforwarded</code> is configured to be true in the
     * application configuration file.
     */
    @Override
    public String remoteAddress() {
        if (headers().containsKey(HeaderNames.X_FORWARD_FOR)) {
            return getHeader(HeaderNames.X_FORWARD_FOR);
        } else {
            InetSocketAddress remote = (InetSocketAddress) channel.channel().remoteAddress();
            return remote.getAddress().getHostAddress();
        }
    }

    /**
     * The request host.
     */
    @Override
    public String host() {
        InetSocketAddress remote = (InetSocketAddress) channel.channel().remoteAddress();
        return remote.getHostName();
    }

    /**
     * The URI path, without the query part.
     */
    @Override
    public String path() {
        try {
            return new URI(request.getUri()).getRawPath();
        } catch (URISyntaxException e) { //NOSONAR
            // Should never be the case.
            return uri();
        }
    }

    /**
     * Get the content media type that is acceptable for the client. E.g. Accept: text/*;q=0.3, text/html;q=0.7,
     * text/html;level=1,text/html;level=2;q=0.4
     * <p/>
     * The Accept request-header field can be used to specify certain media
     * types which are acceptable for the response. Accept headers can be used
     * to indicate that the request is specifically limited to a small set of
     * desired types, as in the case of a request for an in-line image.
     *
     * @return a MediaType that is acceptable for the
     *         client or {@see MediaType#HTML_UTF_8} if not set
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public MediaType mediaType() {
        String contentType = request.headers().get(HeaderNames.ACCEPT);

        if (contentType == null) {
            // HTML by default
            return MediaType.HTML_UTF_8;
        }

        MediaType type = MediaType.parse(contentType);

        if (type == null) {
            return MediaType.HTML_UTF_8;
        } else if (type.equals(MediaType.ANY_TYPE)) {
            return MediaType.HTML_UTF_8;
        } else {
            return type;
        }
    }

    /**
     * Check if this request accepts a given media type.
     *
     * @return true if <code>mimeType</code> is in the Accept header, otherwise false
     */
    @Override
    public boolean accepts(String mimeType) {
        String contentType = request.headers().get(HeaderNames.ACCEPT);
        if (contentType == null) {
            contentType = MimeTypes.HTML;
        }
        return contentType.contains(mimeType);
    }

    /**
     * @return the request cookies
     */
    @Override
    public Cookies cookies() {
        return cookies;
    }

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    /**
     * Retrieves all headers.
     *
     * @return headers
     */
    @Override
    public Map<String, List<String>> headers() {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        final HttpHeaders requestHeaders = request.headers();
        Set<String> names = requestHeaders.names();
        for (String name : names) {
            headers.put(name, requestHeaders.getAll(name));
        }
        return headers;
    }
}
