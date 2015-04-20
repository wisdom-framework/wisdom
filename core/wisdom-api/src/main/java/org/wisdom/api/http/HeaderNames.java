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
 * Defines all standard HTTP headers.
 */
public interface HeaderNames {

    String ACCEPT = "Accept";
    String ACCEPT_CHARSET = "Accept-Charset";
    String ACCEPT_ENCODING = "Accept-Encoding";
    String ACCEPT_LANGUAGE = "Accept-Language";
    String ACCEPT_RANGES = "Accept-Ranges";
    String AGE = "Age";
    String ALLOW = "Allow";
    String AUTHORIZATION = "Authorization";
    String CACHE_CONTROL = "Cache-Control";
    String NOCACHE_VALUE = "no-cache";
    String CONNECTION = "Connection";
    String CONTENT_DISPOSITION = "Content-Disposition";

    String CONTENT_ENCODING = "Content-Encoding";
    String CONTENT_LANGUAGE = "Content-Language";
    String CONTENT_LENGTH = "Content-Length";
    String CONTENT_LOCATION = "Content-Location";
    String CONTENT_MD5 = "Content-MD5";
    String CONTENT_RANGE = "Content-Range";
    String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    String CONTENT_TYPE = "Content-Type";
    String COOKIE = "Cookie";
    String DATE = "Date";
    String ETAG = "Etag";
    String EXPECT = "Expect";
    String EXPIRES = "Expires";
    String FROM = "From";
    String HOST = "Host";
    String IF_MATCH = "If-Match";
    String IF_MODIFIED_SINCE = "If-Modified-Since";
    String IF_NONE_MATCH = "If-None-Match";
    String IF_RANGE = "If-Range";
    String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    String LAST_MODIFIED = "Last-Modified";
    String LOCATION = "Location";
    String MAX_FORWARDS = "Max-Forwards";
    String PRAGMA = "Pragma";
    String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    String PROXY_AUTHORIZATION = "Proxy-Authorization";
    String RANGE = "Range";
    String REFERER = "Referer";
    String RETRY_AFTER = "Retry-After";
    String SERVER = "Server";
    String SET_COOKIE = "Set-Cookie";
    String SET_COOKIE2 = "Set-Cookie2";
    String TE = "Te";
    String TRAILER = "Trailer";
    String TRANSFER_ENCODING = "Transfer-Encoding";
    String UPGRADE = "Upgrade";
    String USER_AGENT = "User-Agent";
    String VARY = "Vary";
    String VIA = "Via";
    String WARNING = "Warning";
    String WWW_AUTHENTICATE = "WWW-Authenticate";
    String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    String ORIGIN = "Origin";
    String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    String X_FORWARD_FOR = "X-Forwarded-For";

    String X_WISDOM_DISABLED_ENCODING_HEADER = "X-Wisdom-Disabled-Encoding";
}
