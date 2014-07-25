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

import org.wisdom.api.utils.KnownMimeTypes;

import java.io.File;
import java.net.URL;

/**
 * Common HTTP MIME types.
 */
public final class MimeTypes {
    
    private MimeTypes(){
        //Hide implicit constructor
    }

    /**
     * Content-Type of text.
     */
    public static final String TEXT = "text/plain";
    /**
     * Content-Type of html.
     */
    public static final String HTML = "text/html";
    /**
     * Content-Type of json.
     */
    public static final String JSON = "application/json";
    /**
     * Content-Type of xml.
     */
    public static final String XML = "application/xml";
    /**
     * Content-Type of css.
     */
    public static final String CSS = "text/css";
    /**
     * Content-Type of javascript.
     */
    public static final String JAVASCRIPT = "text/javascript";
    /**
     * Content-Type of form-urlencoded.
     */
    public static final String FORM = "application/x-www-form-urlencoded";
    /**
     * Content-Type of server sent events.
     */
    public static final String EVENT_STREAM = "text/event-stream";
    /**
     * Content-Type of binary data.
     */
    public static final String BINARY = "application/octet-stream";

    /**
     * Multipart.
     */
    public static final String MULTIPART = "multipart/form-data";

    public static String getMimeTypeForFile(File file) {
        if (file == null) {
            //The input file is null so we can't retrieve a mimetype, therefore we return null.
            return null;
        }
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
        if (url == null) {
            //The input url is null so we can't retrieve a mimetype, therefore we return null.
            return null;
        }
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
