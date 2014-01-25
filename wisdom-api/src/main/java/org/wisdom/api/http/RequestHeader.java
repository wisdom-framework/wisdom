package org.wisdom.api.http;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;

import com.google.common.net.MediaType;

public abstract class RequestHeader {
    /**
     * Regex to parse a segment of the ACCEPT-LANGUAGE header.
     * The group #1 contains the locale tag, while the group #5 contains the `q` value.
     */
    private static final Pattern LANGUAGE_SEGMENT_PATTERN = Pattern.compile("([a-zA-Z]+(-[a-zA-Z]+)?(-[a-zA-Z]+)?)(;q=(.*))?");
    
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
            if(headerName.equalsIgnoreCase(h)) {
                headers = headers().get(h);
                break;
            }
        }
        if(headers == null || headers.isEmpty()) {
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
     * Get the locale that are acceptable for the client. E.g. Accept-Language:
     * da, en-gb;q=0.8, en;q=0.7
     *
     * The Accept-Language request-header field is similar to Accept, but
     * restricts the set of natural languages that are preferred as a response
     * to the request.
     *
     * This method builds an ordered list of locale (favorite first).
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     *
     * @return the set of locale that are acceptable for the client in the preference order.
     */
    public Locale[] languages() {
        return getLocaleList(getHeader(HeaderNames.ACCEPT_LANGUAGE));
    }

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

    /**
     * Builds the list of locale in the preference order accepted by the client. For reminder,
     * the ACCEPT-LANGUAGE header follows this convention:
     * <code>
     *     <pre>
     *         Accept-Language = "Accept-Language" ":"
     *         1#( language-range [ ";" "q" "=" qvalue ] )
     *         language-range  = ( ( 1*8ALPHA *( "-" 1*8ALPHA ) ) | "*" )
     *     </pre>
     * </code>
     * @param accept the ACCEPT-LANGUAGE header value
     * @return the list of locale, empty if the header is {@literal null} or non-parseable
     * @see RequestHeader#languages()
     */
    public static Locale[] getLocaleList(String accept) {
        if (accept == null  || accept.length() == 0) {
            return new Locale[0];
        }

        Map<Float, List<Locale>> locales = new TreeMap<>(new Comparator<Float>() {
            @Override
            public int compare(Float o1, Float o2) {
                return o2.compareTo(o1);
            }
        });
        String[] segments = accept.split(",");
        for (String segment : segments) {
            Matcher matcher = LANGUAGE_SEGMENT_PATTERN.matcher(segment.trim());
            if (! matcher.matches()) {
                continue;
            }
            float q = 1;
            if (matcher.group(5) != null) {
                q = Float.valueOf(matcher.group(5));
            }
            List<Locale> l = locales.get(q);
            if (l == null) {
                l = new ArrayList<>();
                locales.put(q, l);
            }
            l.add(Locale.forLanguageTag(matcher.group(1)));
        }

        // Now iterates from highest q to lowest.
        List<Locale> list = new ArrayList<>();
        for (Map.Entry<Float, List<Locale>> entry : locales.entrySet()) {
            list.addAll(entry.getValue());
        }
        return list.toArray(new Locale[list.size()]);
    }
}
