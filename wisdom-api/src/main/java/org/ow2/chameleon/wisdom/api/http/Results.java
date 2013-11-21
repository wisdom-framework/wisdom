package org.ow2.chameleon.wisdom.api.http;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.ow2.chameleon.wisdom.api.bodies.NoHttpBody;
import org.ow2.chameleon.wisdom.api.bodies.RenderableFile;
import org.ow2.chameleon.wisdom.api.bodies.RenderableURL;

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
public class Results {

    public static Result status(int statusCode) {
        return new Result(statusCode);
    }

    public static Result ok() {
        return status(Result.OK).render(new NoHttpBody());
    }

    public static Result ok(ObjectNode object) {
        return status(Result.OK).render(object).as(MimeTypes.JSON);
    }

    public static Result ok(Object object) {
        return status(Result.OK).render(object);
    }

    public static Result ok(URL object) {
        return status(Result.OK).render(new RenderableURL(object));
    }

    public static Result ok(String object) {
        return status(Result.OK).render(object).as(MimeTypes.TEXT);
    }

    public static Result ok(Result result) {
        return result.status(Result.OK);
    }

    public static Result notFound() {
        return status(Result.NOT_FOUND).noContentIfNone();
    }

    public static Result notFound(String content) {
        return status(Result.NOT_FOUND).render(content);
    }

    public static Result notFound(Result result) {
        return result.status(Result.NOT_FOUND);
    }

    public static Result notFound(Renderable renderable) {
        return status(Result.NOT_FOUND).render(renderable);
    }

    public static Result forbidden() {
        return status(Result.FORBIDDEN).noContentIfNone();
    }

    public static Result forbidden(Result result) {
        return result.status(Result.FORBIDDEN);
    }

    public static Result badRequest() {
        return status(Result.BAD_REQUEST).noContentIfNone();
    }

    public static Result badRequest(Result result) {
        return result.status(Result.BAD_REQUEST);
    }

    public static Result badRequest(Object object) {
        return status(Result.BAD_REQUEST).render(object);
    }

    public static Result noContent() {
        return status(Result.NO_CONTENT)
                .render(new NoHttpBody());
    }

    public static Result internalServerError() {
        return status(Result.INTERNAL_SERVER_ERROR).noContentIfNone();
    }

    public static Result internalServerError(Throwable e) {
        return status(Result.INTERNAL_SERVER_ERROR).render(e).as(MimeTypes.JSON);
    }

    public static Result internalServerError(String content) {
        return status(Result.INTERNAL_SERVER_ERROR).render(content).as(MimeTypes.TEXT);
    }

    public static Result internalServerError(Renderable renderable) {
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
     *         as Location header. Renders no Http body by default.
     */
    public static Result redirect(String url) {
        return status(Result.SEE_OTHER)
                .with(Response.LOCATION, url)
                .render(new NoHttpBody());
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
     *         as Location header. Renders no Http body by default.
     */
    public static Result redirectTemporary(String url) {
        return status(Result.TEMPORARY_REDIRECT)
                .with(Response.LOCATION, url)
                .render(new NoHttpBody());
    }

    public static Result html() {
        return status(Result.OK).as(MimeTypes.HTML);
    }

    public static Result json() {
        return status(Result.OK).as(MimeTypes.JSON);
    }

    public static Result xml() {
        return status(Result.OK).as(MimeTypes.XML);
    }

    public static Result TODO() {
        return status(Result.NOT_IMPLEMENTED).as(MimeTypes.JSON);
    }

    public static Result ok(File file, boolean attachment) {
        Preconditions.checkArgument(file.exists());
        Result result = status(Result.OK)
                .as(MimeTypes.getMimeTypeForFile(file))
                .with(Response.CONTENT_LENGTH, Long.toString(file.length()))
                .render(new RenderableFile(file));
        if (attachment) {
            // Add the content-disposal header
            result.with(Response.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
        }
        return result;
    }

    public static Result ok(File file) {
        return ok(file, false);
    }

    public static AsyncResult async(Callable<Result> task) {
        return new AsyncResult(task);
    }

}
