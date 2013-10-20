package org.ow2.chameleon.wisdom.api.http;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 20/10/13
 * Time: 08:01
 * To change this template use File | Settings | File Templates.
 */
public class RenderableFile implements Renderable {


    private final File file;

    public RenderableFile(File file) {
        this.file = file;
    }

    @Override
    public void render(Context context, Result result, Map<String, Object> params) throws Exception {
        OutputStream stream = context.response().stream();
        FileUtils.copyFile(file, stream);
    }
}
