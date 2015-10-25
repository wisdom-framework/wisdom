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
 * Defines all standard HTTP status codes.
 */
public interface Status {

    int CONTINUE = 100;
    int SWITCHING_PROTOCOLS = 101;
    int OK = 200;
    int CREATED = 201;
    int ACCEPTED = 202;
    int NON_AUTHORITATIVE_INFORMATION = 203;
    int NO_CONTENT = 204;
    int RESET_CONTENT = 205;
    int PARTIAL_CONTENT = 206;
    int PAYLOAD_TOO_LARGE = 413;
    int MULTIPLE_CHOICES = 300;
    int MOVED_PERMANENTLY = 301;
    int FOUND = 302;
    int SEE_OTHER = 303;
    int NOT_MODIFIED = 304;
    int USE_PROXY = 305;
    int TEMPORARY_REDIRECT = 307;
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int PAYMENT_REQUIRED = 402;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int METHOD_NOT_ALLOWED = 405;
    int NOT_ACCEPTABLE = 406;
    int PROXY_AUTHENTICATION_REQUIRED = 407;
    int REQUEST_TIMEOUT = 408;
    int CONFLICT = 409;
    int GONE = 410;
    int LENGTH_REQUIRED = 411;
    int PRECONDITION_FAILED = 412;
    /**
     * @deprecated use Payload Too Large (RFC 7231) instead
     */
    @Deprecated
    int REQUEST_ENTITY_TOO_LARGE = 413;
    int REQUEST_URI_TOO_LONG = 414;
    int UNSUPPORTED_MEDIA_TYPE = 415;
    int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    int EXPECTATION_FAILED = 417;
    int INTERNAL_SERVER_ERROR = 500;
    int NOT_IMPLEMENTED = 501;
    int BAD_GATEWAY = 502;
    int SERVICE_UNAVAILABLE = 503;
    int GATEWAY_TIMEOUT = 504;
    int HTTP_VERSION_NOT_SUPPORTED = 505;
}