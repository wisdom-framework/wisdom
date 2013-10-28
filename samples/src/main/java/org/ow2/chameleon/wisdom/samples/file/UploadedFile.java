package org.ow2.chameleon.wisdom.samples.file;

import java.io.File;

/**
 * File Item
 */
public class UploadedFile {

    public final String name;
    public final long size;
    public final String url;

    public UploadedFile(File file, String url) {
        this.name = file.getName();
        this.size = file.length();
        this.url = url;
    }
}
