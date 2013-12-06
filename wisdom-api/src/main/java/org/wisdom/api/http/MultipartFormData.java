package org.wisdom.api.http;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Multipart form data body.
 */
public abstract class MultipartFormData {

    /**
     * Extract the data parts as Form url encoded.
     */
    public abstract Map<String, String[]> asFormUrlEncoded();

    /**
     * Retrieves all file parts.
     */
    public abstract List<FilePart> getFiles();

    /**
     * Access a file part.
     */
    public FilePart getFile(String key) {
        for (FilePart filePart : getFiles()) {
            if (filePart.getKey().equals(key)) {
                return filePart;
            }
        }
        return null;
    }

    /**
     * A file part.
     */
    public static class FilePart {

        final String key;
        final String filename;
        final String contentType;
        final File file;

        public FilePart(String key, String filename, String contentType, File file) {
            this.key = key;
            this.filename = filename;
            this.contentType = contentType;
            this.file = file;
        }

        /**
         * The part name.
         */
        public String getKey() {
            return key;
        }

        /**
         * The file name.
         */
        public String getFilename() {
            return filename;
        }

        /**
         * The file Content-Type
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * The File.
         */
        public File getFile() {
            return file;
        }

    }

}
