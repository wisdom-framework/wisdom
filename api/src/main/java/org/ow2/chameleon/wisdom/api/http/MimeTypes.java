package org.ow2.chameleon.wisdom.api.http;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Common HTTP MIME types
 */
public class MimeTypes {

    private static final String PROPERTY_MIME_TYPE_PREFIX = "mimetype.";
    private static final String DEFAULT_MIME_TYPE_LOCATIONS = "org/ow2/chameleon/wisdom.api/http/mime-types.properties";

    private static Properties mimetypes;

    static {
        InputStream is = MimeTypes.class.getClassLoader()
                .getResourceAsStream(DEFAULT_MIME_TYPE_LOCATIONS);
        try {
            mimetypes.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        IOUtils.closeQuietly(is);
    }

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

    public static String getMimeTypeForFile(File file) {
        if (file.getName().indexOf('.') == -1) {
            return BINARY;
        } else {
            String ext = file.getName().substring(file.getName().indexOf('.') + 1);
            String mime = mimetypes.getProperty(ext);
            if (mime == null) {
                return BINARY;
            } else {
                return mime;
            }
        }
    }
}
