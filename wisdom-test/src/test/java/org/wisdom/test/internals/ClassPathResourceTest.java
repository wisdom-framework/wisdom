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
package org.wisdom.test.internals;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the creation of BND Resources from Guava ClassPath Resources
 */
public class ClassPathResourceTest {

    @Test
    public void test() throws Exception {
        File file = new File("src/test/resources/foo.txt");
        ClassPath.ResourceInfo info = getResourceForName("foo.txt");
        assertThat(info).isNotNull();
        ClassPathResource resource = new ClassPathResource(info);
        assertThat(resource.getExtra()).isNullOrEmpty();
        assertThat(resource.lastModified()).isGreaterThanOrEqualTo(file.lastModified());
        assertThat(resource.size()).isEqualTo(file.length());

        InputStream stream = resource.openInputStream();
        String content = IOUtils.toString(stream);
        IOUtils.closeQuietly(stream);

        assertThat(content).isEqualTo(FileUtils.readFileToString(file));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resource.write(bos);
        content = bos.toString();
        IOUtils.closeQuietly(bos);

        assertThat(content).isEqualTo(FileUtils.readFileToString(file));

    }

    private static ClassPath.ResourceInfo getResourceForName(String name) throws IOException {
        ClassPath cp = ClassPath.from(ClassPathResourceTest.class.getClassLoader());
        ImmutableSet<ClassPath.ResourceInfo> resources = cp.getResources();
        for (ClassPath.ResourceInfo info : resources) {
            if (info.getResourceName().equals(name)) {
                return info;
            }
        }
        return null;
    }

}
