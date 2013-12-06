package org.wisdom.api.http;

import org.wisdom.api.utils.KnownMimeTypes;

import java.io.File;
import java.net.URL;

/**
 * Common HTTP MIME types
 */
public class MimeTypes {

    /**
     * Content-Type of text.
     */
    public final static String TEXT = "text/plain";
    /**
     * Content-Type of html.
     */
    public final static String HTML = "text/html";
    /**
     * Content-Type of json.
     */
    public final static String JSON = "application/json";
    /**
     * Content-Type of xml.
     */
    public final static String XML = "application/xml";
    /**
     * Content-Type of css.
     */
    public final static String CSS = "text/css";
    /**
     * Content-Type of javascript.
     */
    public final static String JAVASCRIPT = "text/javascript";
    /**
     * Content-Type of form-urlencoded.
     */
    public final static String FORM = "application/x-www-form-urlencoded";
    /**
     * Content-Type of server sent events.
     */
    public final static String EVENT_STREAM = "text/event-stream";
    /**
     * Content-Type of binary data.
     */
    public final static String BINARY = "application/octet-stream";

    /**
     * Multipart.
     */
    public final static String MULTIPART = "multipart/form-data";

    public static String getMimeTypeForFile(File file) {
        String name = file.getName();
        if (name.indexOf('.') == -1) {
            return BINARY;
        } else {
            String ext = name.substring(name.lastIndexOf('.') + 1);
            String mime = KnownMimeTypes.getMimeTypeByExtension(ext);
            if (mime == null) {
                return BINARY;
            } else {
                return mime;
            }
        }
    }

    /**
     * Makes an educated guess of the mime type of the resource pointed by this url.
     * It tries to extract an 'extension' part and confronts this extension to the list of known extensions.
     * @param url the url
     * @return the mime type, BINARY if not found.
     */
    public static String getMimeTypeForFile(URL url) {
        String external = url.toExternalForm();
        if (external.indexOf('.') == -1) {
            return BINARY;
        } else {
            String ext = external.substring(external.lastIndexOf('.') + 1);
            String mime = KnownMimeTypes.getMimeTypeByExtension(ext);
            if (mime == null) {
                return BINARY;
            } else {
                return mime;
            }
        }
    }
}
