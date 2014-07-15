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
package org.wisdom.api.http;

import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class MimeTypesTest {

    @Test
    public void testGetMimeTypeForFile() throws Exception {
        File myFile = new File("ReadMe");
        assertThat(MimeTypes.getMimeTypeForFile(myFile)).isEqualTo(MimeTypes.BINARY);

        assertThat(MimeTypes.getMimeTypeForFile((File) null)).isEqualTo(null);

        myFile = new File("ReadMe.kitten");
        assertThat(MimeTypes.getMimeTypeForFile(myFile)).isEqualTo(MimeTypes.BINARY);

        myFile = new File("ReadMe.jpg");
        assertThat(MimeTypes.getMimeTypeForFile(myFile)).isEqualTo("image/jpeg");
    }

    @Test
    public void testGetMimeTypeForFileWithURL() throws Exception {
        URL url = new URL("http://localhost:9000/ReadMe");
        assertThat(MimeTypes.getMimeTypeForFile(url)).isEqualTo(MimeTypes.BINARY);
        assertThat(MimeTypes.getMimeTypeForFile((URL)null)).isEqualTo(null);
        url = new URL("http://localhost:9000/ReadMe.kitten");
        assertThat(MimeTypes.getMimeTypeForFile(url)).isEqualTo(MimeTypes.BINARY);
        url = new URL("http://localhost:9000/ReadMe.txt");
        assertThat(MimeTypes.getMimeTypeForFile(url)).isEqualTo("text/plain");
    }
}