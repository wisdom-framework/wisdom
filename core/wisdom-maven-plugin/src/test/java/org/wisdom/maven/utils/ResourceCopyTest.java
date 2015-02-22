/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.maven.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceCopyTest {


    @Test
    public void testTheListOfNonFilteredExtension() {

        // Images
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("bmp");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("jpg");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("jpeg");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("tiff");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("png");

        // Document
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("pdf");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("doc");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("dot");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("docx");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("xls");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("xlsx");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("ppt");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("pptx");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("pps");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("ogg");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("mp3");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("mp4");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("avi");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("mpeg");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("swf");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("key");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("flv");

        // Archive
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("zip");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("tar");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("tar.gz");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("gz");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("tgz");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("jar");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("war");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("nar");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("rar");
        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("bz2");

        assertThat(ResourceCopy.NON_FILTERED_EXTENSIONS).contains("jks");
    }

}