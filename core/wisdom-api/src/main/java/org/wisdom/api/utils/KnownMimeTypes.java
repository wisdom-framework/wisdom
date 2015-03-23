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
package org.wisdom.api.utils;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import com.google.common.net.MediaType;
import org.apache.commons.io.IOUtils;
import org.wisdom.api.http.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * A list of known mime types by extensions.
 * This list is built in two steps:
 * First the a mime type file is read associating mime types to extension.
 * Then, another map is built associating extensions to mime type. This second map is build for performance reason.
 */
public final class KnownMimeTypes {

    /**
     * The map associating mime type to extensions
     */
    public static final Multimap<String, String> EXTENSIONS_FOR_MIME = TreeMultimap.create();

    /**
     * The map associating extension to mime-types.
     * This map is constructed to improve performance.
     */
    public static final Map<String, String> EXTENSIONS = new TreeMap<>();

    /**
     * The lists of extension which are 'archives'.
     */
    public static final Set<String> COMPRESSED_MIME;  //NOSONAR


    private KnownMimeTypes() {
        //Hide implicit constructor
    }

    static {
        // Load the known mime type file and build the list of extensions.
        URL url = KnownMimeTypes.class.getClassLoader().getResource("mimes/known.mime.types");
        Preconditions.checkNotNull(url, "Cannot find the internal known mime types");
        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = url.openStream();
            properties.load(is);
            for (String key: properties.stringPropertyNames()) {
                String extensions = properties.getProperty(key);
                final Iterable<String> listOfExtensions =
                        Splitter.on(" ").trimResults().split(extensions);
                EXTENSIONS_FOR_MIME.putAll(key, listOfExtensions);
                for (String ext : listOfExtensions) {
                    EXTENSIONS.put(ext, key);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load the internal known mime types", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Builds the list of extension that are used by archive formats, such as zip, bz...
     */
    static {
        //From http://en.wikipedia.org/wiki/List_of_archive_formats
        COMPRESSED_MIME = new HashSet<>();
        addMimeToCompressedWithExtension("bz2");
        addMimeToCompressedWithExtension("gz");
        addMimeToCompressedWithExtension("gzip");
        addMimeToCompressedWithExtension("lzma");
        addMimeToCompressedWithExtension("z");
        addMimeToCompressedWithExtension("7z");
        addMimeToCompressedWithExtension("s7z");
        addMimeToCompressedWithExtension("ace");
        addMimeToCompressedWithExtension("alz");
        addMimeToCompressedWithExtension("arc");
        addMimeToCompressedWithExtension("arj");
        addMimeToCompressedWithExtension("cab");
        addMimeToCompressedWithExtension("cpt");
        addMimeToCompressedWithExtension("dar");
        addMimeToCompressedWithExtension("dmg");
        addMimeToCompressedWithExtension("ice");
        addMimeToCompressedWithExtension("lha");
        addMimeToCompressedWithExtension("lzx");
        addMimeToCompressedWithExtension("rar");
        addMimeToCompressedWithExtension("sit");
        addMimeToCompressedWithExtension("sitx");
        addMimeToCompressedWithExtension("tar");
        addMimeToCompressedWithExtension("tgz");
        addMimeToCompressedWithExtension("zip");
        addMimeToCompressedWithExtension("zoo");

        addMimeGroups("video/", "image/", "audio/");
    }

    /**
     * Adds a mime-type to the compressed list.
     *
     * @param extension the extension, without the "."
     */
    private static void addMimeToCompressedWithExtension(String extension) {
        String mime = EXTENSIONS.get(extension);
        if (mime != null && !COMPRESSED_MIME.contains(mime)) {
            COMPRESSED_MIME.add(mime);
        }
    }

    /**
     * Adds a group to the compressed list.
     *
     * @param groups the groups
     */
    private static void addMimeGroups(String... groups) {
        for (String mimeType : EXTENSIONS.values()) {
            for (String group : groups) {
                if (mimeType.startsWith(group) && !COMPRESSED_MIME.contains(mimeType)) {
                    COMPRESSED_MIME.add(mimeType);
                }
            }
        }
    }

    /**
     * Gets a mime-type for the extension of a file or url.
     *
     * @param extension the extension, without the "."
     * @return the mime-type if known, {@literal null} otherwise.
     */
    public static String getMimeTypeByExtension(String extension) {
        return EXTENSIONS.get(extension);
    }

    /**
     * Gets a {@link MediaType} for the extension of a file or url.
     *
     * @param extension the extension, without the "."
     * @return the parsed media type if known, {@literal null} otherwise.
     * @since 0.8.1
     */
    public static MediaType getMediaTypeByExtension(String extension) {
        final String input = EXTENSIONS.get(extension);
        if (input == null) {
            return null;
        }
        return MediaType.parse(input);
    }
}
