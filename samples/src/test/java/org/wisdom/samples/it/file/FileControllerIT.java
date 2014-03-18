package org.wisdom.samples.it.file;


import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.samples.file.FileController;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.ControllerTest;
import org.wisdom.test.parents.Invocation;

import javax.inject.Inject;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * Integration test about the file upload controller.
 */
public class FileControllerIT extends ControllerTest {

    final File file = new File("src/test/resources/OneWeek.pdf");

    @Inject
    FileController controller;

    @Test
    public void index() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() {
                return controller.index();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo(MimeTypes.HTML);
        assertThat(toString(result)).contains("My Wisdom-based file server");
    }

    @Test
    public void upload() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Exception {
                return controller.upload(from(file));
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo(MimeTypes.HTML);
        assertThat(toString(result)).contains("My Wisdom-based file server");
        assertThat(toString(result)).contains("File " + file.getName() + " uploaded (" + file.length() + " bytes)");
    }

    @Test
    public void download() {
        // First upload a file.
        upload();

        // Download the file
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Exception {
                return controller.download(file.getName());
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo(MimeTypes.getMimeTypeForFile(file));
        assertThat((long) toBytes(result).length).isEqualTo(file.length());

    }

    @Test
    public void downloadMissingFile() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Exception {
                return controller.download("missing");
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void uploadWithoutFile() {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Exception {
                return controller.upload(null);
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

}
