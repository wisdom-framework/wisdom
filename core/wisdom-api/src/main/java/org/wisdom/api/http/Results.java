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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import org.w3c.dom.Document;
import org.wisdom.api.bodies.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;


/**
 * Convenience methods for the generation of Results.
 * <p/>
 * {@link Results#forbidden() generates a results and sets it to forbidden.
 * <p/>
 * A range of shortcuts are available from here.
 */
public abstract class Results {

    /**
     * Generates a simple result with the given status.
     *
     * @param statusCode the status
     * @return a result with the given status, but without content.
     */
    public static Result status(int statusCode) {
        return new Result(statusCode);
    }

    /**
     * Generates a simple result with the status {@literal 200 - OK}.
     *
     * @return a new result with the status 200, with an empty content.
     */
    public static Result ok() {
        return status(Result.OK).render(NoHttpBody.INSTANCE);
    }

    /**
     * Generates a result with the {@literal 200 - OK} status and with the given XML content. The result has the
     * {@literal Content-Type} header set to {@literal application/xml}.
     *
     * @param document the XML document
     * @return a new configured result
     */
    public static Result ok(Document document) {
        return status(Result.OK).render(document).as(MimeTypes.XML);
    }

    /**
     * Generates a result with the {@literal 200 - OK} status and with the given JSON content. The result has the
     * {@literal Content-Type} header set to {@literal application/json}.
     *
     * @param node the json object (JSON array or JSON object)
     * @return a new configured result
     */
    public static Result ok(JsonNode node) {
        return status(Result.OK).render(node).as(MimeTypes.JSON);
    }

    /**
     * Generates a result with the status {@literal 200 - OK} and with the given content. Depending of the type
     * of the given object, the content will be preprocessed.
     *
     * @param object the content
     * @return a new configured result
     */
    public static Result ok(Object object) {
        return status(Result.OK).render(object);
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} with the content loaded from the given URL. Wisdom
     * tries to guess the  {@literal Content-Type} header value from the given URL. However,
     * we recommend to set it explicitly, to avoid bad guesses.
     *
     * @param object the url
     * @return a new configured result
     */
    public static Result ok(URL object) {
        return status(Result.OK).render(new RenderableURL(object));
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} with the content loaded from the given input stream.
     *
     * @param object the stream
     * @return a new configured result
     */
    public static Result ok(InputStream object) {
        return status(Result.OK).render(new RenderableStream(object));
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} with the content loaded from the
     * given byte array. The result is sent as chunked.
     *
     * @param bytes the byte array, must not be {@code null}
     * @return a new configured result
     */
    public static Result ok(byte[] bytes) {
        return status(Result.OK)
                .render(new RenderableByteArray(bytes, true));
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} with the content loaded from the
     * given byte array. The result is sent as chunked.
     *
     * @param bytes   the byte array, must not be {@code null}
     * @param chunked whether or not the result is sent as chunks
     * @return a new configured result
     */
    public static Result ok(byte[] bytes, boolean chunked) {
        return status(Result.OK)
                .render(new RenderableByteArray(bytes, chunked));
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} with the given content. The result has the
     * {@literal Content-Type} header set to {@literal text/plain}.
     *
     * @param object the content
     * @return a new configured result
     */
    public static Result ok(String object) {
        return status(Result.OK).render(object).as(MimeTypes.TEXT);
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} building a JSONP response.
     *
     * @param padding the callback name
     * @param node    the json object (JSON array or JSON object)
     * @return the JSONP response built as follows: padding(node)
     */
    public static Result ok(String padding, JsonNode node) {
        return status(Result.OK).render(padding, node);
    }

    /**
     * Sets the status of the given result to {@literal 200 - OK}.
     *
     * @param result the result to update
     * @return the given result with the updated status
     */
    public static Result ok(Result result) {
        return result.status(Result.OK);
    }

    /**
     * Generates a new result with an empty content, and with the status set to {@literal 404 - NOT FOUND}.
     *
     * @return the new result
     */
    public static Result notFound() {
        return status(Result.NOT_FOUND).noContentIfNone();
    }

    /**
     * Creates a new result with the status {@literal 404 - NOT FOUND} with the given content. The result has the
     * {@literal Content-Type} header set to {@literal text/plain}.
     *
     * @param content the content
     * @return a new configured result
     */
    public static Result notFound(String content) {
        return status(Result.NOT_FOUND).render(content).as(MimeTypes.TEXT);
    }

    /**
     * Sets the status of the given result to {@literal 404 - NOT FOUND}.
     *
     * @param result the result to update
     * @return the given result with the updated status
     */
    public static Result notFound(Result result) {
        return result.status(Result.NOT_FOUND);
    }

    /**
     * Creates a new result with the status {@literal 404 - NOT FOUND} with the given content. The {@literal
     * Content-Type} header value is deduced from the given content.
     *
     * @param renderable the content
     * @return a new configured result
     */
    public static Result notFound(Renderable<?> renderable) {
        return status(Result.NOT_FOUND).render(renderable);
    }

    /**
     * Generates a new result with an empty content, and with the status set to {@literal 403 - FORBIDDEN}.
     *
     * @return the new result
     */
    public static Result forbidden() {
        return status(Result.FORBIDDEN).noContentIfNone();
    }

    /**
     * Sets the status of the given result to {@literal 403 - FORBIDDEN}.
     *
     * @param result the result to update
     * @return the given result with the updated status
     */
    public static Result forbidden(Result result) {
        return result.status(Result.FORBIDDEN);
    }

    /**
     * Creates a new result with the status {@literal 403 - FORBIDDEN} with the given content. The result has the
     * {@literal Content-Type} header set to {@literal text/plain}.
     *
     * @param content the content
     * @return a new configured result
     */
    public static Result forbidden(String content) {
        return status(Result.FORBIDDEN).render(content).as(MimeTypes.TEXT);
    }

    /**
     * Generates a new result with an empty content, and with the status set to {@literal 401 - UNAUTHORIZED}.
     *
     * @return the new result
     */
    public static Result unauthorized() {
        return status(Result.UNAUTHORIZED).noContentIfNone();
    }

    /**
     * Sets the status of the given result to {@literal 401 - UNAUTHORIZED}.
     *
     * @param result the result to update
     * @return the given result with the updated status
     */
    public static Result unauthorized(Result result) {
        return result.status(Result.UNAUTHORIZED);
    }

    /**
     * Creates a new result with the status {@literal 401 - UNAUTHORIZED} with the given content. The result has the
     * {@literal Content-Type} header set to {@literal text/plain}.
     *
     * @param content the content
     * @return a new configured result
     */
    public static Result unauthorized(String content) {
        return status(Result.UNAUTHORIZED).render(content).as(MimeTypes.TEXT);
    }

    /**
     * Generates a new result with an empty content, and with the status set to {@literal 400 - BAD REQUEST}.
     *
     * @return the new result
     */
    public static Result badRequest() {
        return status(Result.BAD_REQUEST).noContentIfNone();
    }

    /**
     * Sets the status of the given result to {@literal 400 - BAD REQUEST}.
     *
     * @param result the result to update
     * @return the given result with the updated status
     */
    public static Result badRequest(Result result) {
        return result.status(Result.BAD_REQUEST);
    }

    /**
     * Generates a result with the status {@literal 400 - BAD REQUEST} and with the given content. Depending of the type
     * of the given object, the content will be preprocessed.
     *
     * @param object the content
     * @return a new configured result
     */
    public static Result badRequest(Object object) {
        return status(Result.BAD_REQUEST).render(object);
    }

    /**
     * Creates a new result with the status {@literal 400 - BAD REQUEST} with the given content. The result has the
     * {@literal Content-Type} header set to {@literal text/plain}.
     *
     * @param content the content
     * @return a new configured result
     */
    public static Result badRequest(String content) {
        return status(Result.BAD_REQUEST).render(content).as(MimeTypes.TEXT);
    }

    /**
     * Generates a new result with the status set to {@literal 204 - NO CONTENT}.
     *
     * @return the new result
     */
    public static Result noContent() {
        return status(Result.NO_CONTENT)
                .render(NoHttpBody.INSTANCE);
    }

    /**
     * Generates a new result with the status set to {@literal 500 - INTERNAL SERVER ERROR} and with an empty content.
     *
     * @return the new result
     */
    public static Result internalServerError() {
        return status(Result.INTERNAL_SERVER_ERROR).noContentIfNone();
    }

    /**
     * Generates a new result with the status set to {@literal 500 - INTERNAL SERVER ERROR} and a JSON-form of the
     * given exception as content. The {@literal Content-Type} header is set to {@literal application/json}.
     *
     * @param e the exception
     * @return the new result
     */
    public static Result internalServerError(Throwable e) {
        return status(Result.INTERNAL_SERVER_ERROR).render(e).as(MimeTypes.JSON);
    }

    /**
     * Generates a new result with the status set to {@literal 500 - INTERNAL SERVER ERROR} with the given text as
     * content. The {@literal Content-Type} header is set to {@literal text/plain}.
     *
     * @param content the content
     * @return the new result
     */
    public static Result internalServerError(String content) {
        return status(Result.INTERNAL_SERVER_ERROR).render(content).as(MimeTypes.TEXT);
    }

    /**
     * Generates a new result with the status set to {@literal 500 - INTERNAL SERVER ERROR} and the given renderable as
     * content.
     *
     * @param renderable the content
     * @return the new result
     */
    public static Result internalServerError(Renderable<?> renderable) {
        return status(Result.INTERNAL_SERVER_ERROR).render(renderable);
    }

    /**
     * A redirect that uses 303 see other.
     * <p/>
     * The redirect does NOT need a template and does NOT
     * render a text in the Http body by default.
     * <p/>
     * If you wish to do so please
     * remove the {@link NoHttpBody} that is set as renderable of
     * the Result.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 303 and the url set
     * as Location header. Renders no Http body by default.
     */
    public static Result redirect(String url) {
        return status(Result.SEE_OTHER)
                .with(HeaderNames.LOCATION, url)
                .render(NoHttpBody.INSTANCE);
    }

    /**
     * A redirect that uses 307 see other.
     * <p/>
     * The redirect does NOT need a template and does NOT
     * render a text in the Http body by default.
     * <p/>
     * If you wish to do so please
     * remove the {@link NoHttpBody} that is set as renderable of
     * the Result.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 307 and the url set
     * as Location header. Renders no Http body by default.
     */
    public static Result redirectTemporary(String url) {
        return status(Result.TEMPORARY_REDIRECT)
                .with(HeaderNames.LOCATION, url)
                .render(NoHttpBody.INSTANCE);
    }

    /**
     * Generates a simple result with the {@literal 200 - OK} and the {@literal Content-Type} set to {@literal
     * text/html}.
     *
     * @return a result with the given status and content type, but without content.
     */
    public static Result html() {
        return status(Result.OK).as(MimeTypes.HTML);
    }

    /**
     * Generates a simple result with the {@literal 200 - OK} and the {@literal Content-Type} set to {@literal
     * application/json}.
     *
     * @return a result with the given status and content type, but without content.
     */
    public static Result json() {
        return status(Result.OK).as(MimeTypes.JSON);
    }

    /**
     * Generates a simple result with the {@literal 200 - OK} and the {@literal Content-Type} set to {@literal
     * application/xml}.
     *
     * @return a result with the given status and content type, but without content.
     */
    public static Result xml() {
        return status(Result.OK).as(MimeTypes.XML);
    }

    /**
     * Generates a simple result with the {@literal 501 - NOT IMPLEMENTED} and the {@literal Content-Type} set to
     * {@literal application/json}.
     *
     * @return a result with the given status and content type, but without content.
     */
    public static Result todo() {
        return status(Result.NOT_IMPLEMENTED).json();
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} sending the given file to the client. Wisdom
     * tries to guess the  {@literal Content-Type} header value from the given file.
     * <p/>
     * If attachment is set to {@literal true}, the {@literal Content-Disposition} header is set to "attachment" and
     * with the file name matching the given file's name.
     *
     * @param file       the file
     * @param attachment whether or not the file need to be sent as attachment.
     * @return a new configured result
     */
    public static Result ok(File file, boolean attachment) {
        Preconditions.checkArgument(file.exists());
        Result result = status(Result.OK)
                .as(MimeTypes.getMimeTypeForFile(file))
                .with(HeaderNames.CONTENT_LENGTH, Long.toString(file.length()))
                .render(new RenderableFile(file));
        if (attachment) {
            // Add the content-disposal header
            result.with(HeaderNames.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
        }
        return result;
    }

    /**
     * Creates a new result with the status {@literal 200 - OK} sending the given file to the client. Wisdom
     * tries to guess the  {@literal Content-Type} header value from the given file.
     *
     * @param file the file
     * @return a new configured result
     */
    public static Result ok(File file) {
        return ok(file, false);
    }

    /**
     * Creates a new async result. The given callable is executed using a separated thread (managed by Wisdom), so,
     * not in the request thread. It is heavily recommended to use async result in each action method needing time to
     * compute the returned result.
     *
     * @param task the callable computing the result.
     * @return the async result.
     */
    public static AsyncResult async(Callable<Result> task) {
        return new AsyncResult(task);
    }

}
