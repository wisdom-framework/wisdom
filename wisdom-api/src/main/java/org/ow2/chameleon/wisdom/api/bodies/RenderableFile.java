package org.ow2.chameleon.wisdom.api.bodies;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;

/**
 * Render a file.
 */
public class RenderableFile implements Renderable {


    private final File file;

    public RenderableFile(File file) {
        this.file = file;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return FileUtils.openInputStream(file);
    }

    @Override
    public long length() {
        return file.length();
    }

    public File getFile() {
        return file;
    }

    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(file);
    }

}
