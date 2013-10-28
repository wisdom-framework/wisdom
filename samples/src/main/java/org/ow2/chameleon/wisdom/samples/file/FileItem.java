package org.ow2.chameleon.wisdom.samples.file;

import java.io.File;

/**
 * File Item
 */
public class FileItem {

    public final String name;
    public final long size;
    public final String url;

    public FileItem(File file, String url) {
        this.name = file.getName();
        this.size = file.length();
        this.url = url;
    }
}
