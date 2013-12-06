package org.wisdom.samples.file;

import java.io.File;

/**
 * File Item
 */
public class UploadedFile {

    public final String name;
    public final long size;

    public UploadedFile(File file) {
        this.name = file.getName();
        this.size = file.length();
    }

    public String getName() {
        return name;
    }
}
