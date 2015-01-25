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
package org.wisdom.test.parents;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.MimeTypes;

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
        assertThat(item.isInMemory()).isFalse();
        assertThat(item.mimetype()).isEqualTo(MimeTypes.TEXT);
        final InputStream stream = item.stream();
        try {
            assertThat(IOUtils.toString(stream)).isEqualTo(content);
        } finally {
            IOUtils.closeQuietly(stream);
        }

        assertThat(item.toFile()).isEqualTo(file);
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

        assertThat(item.toFile()).isEqualTo(file);
    }

}
