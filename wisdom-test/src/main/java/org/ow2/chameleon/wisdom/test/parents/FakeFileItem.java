package org.ow2.chameleon.wisdom.test.parents;

import org.apache.commons.io.FileUtils;
import org.ow2.chameleon.wisdom.api.http.FileItem;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;

import java.io.*;

/**
 *
 */
public class FakeFileItem implements FileItem {

    private final File file;
    private final String field;

    public FakeFileItem(File file, String field) {
        this.file = file;
        this.field = field;
    }

    @Override
    public String field() {
        return field;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public byte[] bytes() {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream stream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(file);
    }

    @Override
    public long size() {
        return file.length();
    }
}
