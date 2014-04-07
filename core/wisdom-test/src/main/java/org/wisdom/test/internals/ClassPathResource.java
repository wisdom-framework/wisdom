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

import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * A bnd resource wrapping a resource from the classpath. The input resources are coming from Google Guava (retrieved
 * using ClassPath analysis).
 */
public class ClassPathResource implements Resource {
    private final ClassPath.ResourceInfo resource;
    private String extra;
    private long modified = System.currentTimeMillis();

    /**
     * Creates the Classpath resource from the given resource.
     *
     * @param resource the wrapped resource
     */
    public ClassPathResource(ClassPath.ResourceInfo resource) {
        this.resource = resource;
    }

    /**
     * Gets an stream on the wrapped resource.
     *
     * @return the stream
     * @throws IOException if the stream cannot be opened
     */
    public InputStream openInputStream() throws IOException {
        return resource.url().openStream();
    }

    /**
     * Adds all the resources found in the given classpath to the given jar. Are excluded resources matching the
     * 'doNotCopy' pattern.
     *
     * @param jar       the jar in which the resources are added
     * @param classpath the classpath
     * @param doNotCopy the do not copy pattern
     */
    public static void build(Jar jar, ClassPath classpath, Pattern doNotCopy) {
        ImmutableSet<ClassPath.ResourceInfo> resources = classpath.getResources();
        for (ClassPath.ResourceInfo resource : resources) {
            if (doNotCopy != null && doNotCopy.matcher(resource.getResourceName()).matches()) {
                continue;
            }
            jar.putResource(resource.getResourceName(), new ClassPathResource(resource));
        }
    }

    /**
     * @return the name of the resources surrounded with ":".
     */
    public String toString() {
        return ":" + resource.getResourceName() + ":";
    }

    /**
     * Writes the content of the current resource to the given output stream.
     *
     * @param out the output stream, not closed by this method
     * @throws Exception if something wrong happened while copying the resource
     */
    public void write(OutputStream out) throws Exception {
        final InputStream input = openInputStream();
        try {
            IOUtils.copy(input, out);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * @return the resource last modified date.
     */
    public long lastModified() {
        return modified;
    }

    /**
     * @return the extra parameter of the resource
     */
    public String getExtra() {
        return extra;
    }

    /**
     * @return the size of the resource. There are no easy way to compute the size of the resource,
     * so it copies the input stream and return the length.
     * @throws Exception if something bad happens.
     */
    @Override
    public long size() throws Exception {
        // There are no easy way to compute the size of the resource, copy the input stream and return the length
        final InputStream stream = resource.url().openStream();
        try {
            return IOUtils.toByteArray(stream).length;
        } finally {
            IOUtils.closeQuietly(stream);
        }

    }

    /**
     * Sets the extra parameter of information about this resource.
     *
     * @param extra the extra
     */
    public void setExtra(String extra) {
        this.extra = extra;
    }
}
