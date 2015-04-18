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
package org.wisdom.test.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.wisdom.api.http.HeaderNames;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * The response to a HTTP request.
 *
 * @param <T> the type of the content.
 */
public class HttpResponse<T> {

    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

    private int code;
    private Map<String, String> headers;
    private InputStream rawBody;
    private T body;
    private int consumedSize;

    /**
     * Creates the response.
     *
     * @param response      the HTTP Client response
     * @param responseClass the class of the response, used to parse the content.
     */
    public HttpResponse(org.apache.http.HttpResponse response, Class<T> responseClass) {
        HttpEntity responseEntity = response.getEntity();

        Header[] allHeaders = response.getAllHeaders();
        // Use a case insensitive map to ease the retrieval of headers.
        this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Header header : allHeaders) {
            headers.put(header.getName().toLowerCase(), header.getValue());
        }
        this.code = response.getStatusLine().getStatusCode();

        parseResponseBody(responseClass, responseEntity);
    }

    @SuppressWarnings("unchecked")
    private void parseResponseBody(Class<T> responseClass, HttpEntity responseEntity) {
        String charset = "UTF-8";
        if (responseEntity != null) {
            try {
                byte[] raw;
                InputStream responseInputStream = responseEntity.getContent();
                if (isGzipped()) {
                    responseInputStream = new GZIPInputStream(responseEntity.getContent());
                }
                raw = getBytes(responseInputStream);
                this.rawBody = new ByteArrayInputStream(raw);
                this.consumedSize = raw.length;

                if (responseEntity.getContentType() != null) {
                    String responseCharset = getCharsetFromContentType(responseEntity.getContentType().getValue());
                    if (responseCharset != null && !responseCharset.trim().equals("")) {
                        charset = responseCharset;
                    }
                }

                if (JsonNode.class.equals(responseClass)) {
                    String jsonString = new String(raw, charset).trim();
                    this.body = (T) new ObjectMapper().readValue(jsonString, JsonNode.class);
                } else if (Document.class.equals(responseClass)) {
                    String r = new String(raw, charset).trim();
                    this.body = (T) Jsoup.parse(r);
                } else if (String.class.equals(responseClass)) {
                    this.body = (T) new String(raw, charset);
                } else if (InputStream.class.equals(responseClass)) {
                    this.body = (T) this.rawBody;
                } else {
                    throw new IllegalArgumentException("Unknown result type. Only String, JsonNode, " +
                            "Document and InputStream are supported.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private boolean isGzipped() {
        Set<Map.Entry<String, String>> heads = headers.entrySet();
        for (Map.Entry<String, String> header : heads) {
            if ("content-encoding".equalsIgnoreCase(header.getKey()) && "gzip".equalsIgnoreCase(header.getValue())) {
                return true;
            }
        }
        return false;
    }

    private static byte[] getBytes(InputStream is) throws IOException {
        return IOUtils.toByteArray(is);
    }

    /**
     * @return the response HTTP status code.
     */
    public int code() {
        return code;
    }

    /**
     * @return the response headers.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * @return the stream to read the content.
     */
    public InputStream raw() {
        return rawBody;
    }

    /**
     * @return the parsed body.
     */
    public T body() {
        return body;
    }

    /**
     * @return the content-type of the response, without the charset.
     */
    public String contentType() {
        String type = headers.get(HeaderNames.CONTENT_TYPE.toLowerCase());
        if (type != null && type.contains(";")) {
            return type.substring(0, type.indexOf(";")).trim();
        }
        return type;
    }

    /**
     * @return the charset of the response. It parses the 'content-type' header, so if this header is not set,
     * {@literal null} is returned.
     */
    public String charset() {
        String type = headers.get(HeaderNames.CONTENT_TYPE.toLowerCase());
        if (type != null && type.contains("charset=")) {
            return type.substring(type.indexOf("charset=") + 8).trim();
        }
        return null;
    }

    /**
     * @return the length of the response body. {@literal -1} is not set. It reads the 'content-length' header.
     */
    public int length() {
        String length = headers.get(HeaderNames.CONTENT_LENGTH.toLowerCase());
        if (length == null && consumedSize == 0) {
            return -1;
        }
        if (length == null) {
            return consumedSize;
        } else {
            return Integer.parseInt(length);
        }
    }

    /**
     * Gets the value of the header.
     *
     * @param name the header's name
     * @return the value, {@literal null} if no value
     */
    public String header(String name) {
        return headers.get(name.toLowerCase());
    }

    /**
     * Retrieves a cookie.
     *
     * @param name the name of the cookie
     * @return the cookie, {@literal null} if not found
     */
    public Cookie cookie(String name) {
        List<Cookie> cookies = ClientFactory.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }



    /**
     * Parse out a charset from a content type header.
     *
     * @param contentType e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and
     * uppercased.
     */
    public static String getCharsetFromContentType(String contentType) {
        if (contentType == null)
            return null;

        Matcher m = CHARSET_PATTERN.matcher(contentType);
        if (m.find()) {
            return m.group(1).trim().toUpperCase();
        }
        return null;
    }
}
