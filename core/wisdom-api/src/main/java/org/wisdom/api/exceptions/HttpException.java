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
package org.wisdom.api.exceptions;

import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;

/**
 * Runtime exception supporting the creation of HTTP {@link Result}.
 */
public class HttpException extends RuntimeException implements Status {

    protected final int status;

    /**
     * Constructs a new instance with a blank message and default HTTP status code of 500.
     */
    public HttpException() {
        this(INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructs a new instance with a blank message and specified HTTP status code.
     *
     * @param status the HTTP status code that will be returned to the client.
     */
    public HttpException(int status) {
        this(status, "");
    }

    /**
     * Constructs a new instance with a message and default HTTP status code of 500.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()}
     *                method).
     */
    public HttpException(String message) {
        this(INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Constructs a new instance with a message and default HTTP status code of 500.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()}
     *                method).
     * @param cause   the underlying cause of the exception.
     */
    public HttpException(String message, Throwable cause) {
        this(INTERNAL_SERVER_ERROR, message, cause);
    }

    /**
     * Constructs a new instance with a blank message and default HTTP status code of 500.
     *
     * @param cause the underlying cause of the exception.
     */
    public HttpException(Throwable cause) {
        this(INTERNAL_SERVER_ERROR, "", cause);
    }

    /**
     * Constructs a new instance with a blank message and specified HTTP status code.
     *
     * @param status the HTTP status code that will be returned to the client.
     * @param cause  the underlying cause of the exception.
     */
    public HttpException(int status, Throwable cause) {
        this(status, "", cause);
    }

    /**
     * Constructs a new instance with the message and specified HTTP status code.
     *
     * @param status  the HTTP status code that will be returned to the client.
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()}
     *                method).
     */
    public HttpException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Constructs a new instance with a blank message and specified HTTP status code.
     *
     * @param status  the HTTP status code that will be returned to the client.
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()}
     *                method).
     * @param cause   the underlying cause of the exception.
     */
    public HttpException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }


    /**
     * Builds the {@link org.wisdom.api.http.Result} instance for the current instance.
     * Notice that the cause is not part of the returned result.
     *
     * @return the result.
     */
    public Result toResult() {
        return new Result().status(status).render(getMessage());
    }
}
