package org.wisdom.test.parents;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wisdom.api.http.FileItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Just check the Fake File Item.
 */
public class FakeFileItemTest {

    File file = new File("src/test/resources/foo.txt");

    @Test
    public void testCreationWithoutField() throws IOException {
        assertThat(file).exists();
        String content = FileUtils.readFileToString(file);

        FileItem item = WisdomTest.from(file);
        assertThat(item.size()).isEqualTo(file.length());
        assertThat(item.name()).isEqualTo(file.getName());
        assertThat(item.field()).isNull();
        assertThat(item.bytes()).isEqualTo(content.getBytes());
        final InputStream stream = item.stream();
        try {
            assertThat(IOUtils.toString(stream)).isEqualTo(content);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Test
    public void testCreationWithField() throws IOException {
        assertThat(file).exists();
        String content = FileUtils.readFileToString(file);

        FileItem item = WisdomTest.from(file, "upload");
        assertThat(item.size()).isEqualTo(file.length());
        assertThat(item.name()).isEqualTo(file.getName());
        assertThat(item.field()).isEqualTo("upload");
        assertThat(item.bytes()).isEqualTo(content.getBytes());
        final InputStream stream = item.stream();
        try {
            assertThat(IOUtils.toString(stream)).isEqualTo(content);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
