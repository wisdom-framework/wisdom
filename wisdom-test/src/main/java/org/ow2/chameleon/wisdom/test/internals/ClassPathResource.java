package org.ow2.chameleon.wisdom.test.internals;

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
 * A bnd resource wrapping a resource from the classpath.
 */
public class ClassPathResource implements Resource {
    private final ClassPath.ResourceInfo resource;
    private String	extra;
    private long modified = System.currentTimeMillis();

        public ClassPathResource(ClassPath.ResourceInfo resource) {
            this.resource = resource;
        }

        public InputStream openInputStream() throws IOException {
            return resource.url().openStream();
        }

        public static void build(Jar jar, ClassPath classpath, Pattern doNotCopy) {
            ImmutableSet<ClassPath.ResourceInfo> resources = classpath.getResources();
            for (ClassPath.ResourceInfo resource : resources) {
                if (doNotCopy != null && doNotCopy.matcher(resource.getResourceName()).matches()) {
                    continue;
                }
                jar.putResource(resource.getResourceName(), new ClassPathResource(resource));
            }
        }

        public String toString() {
            return ":" + resource.getResourceName() + ":";
        }

        public void write(OutputStream out) throws Exception {
            IOUtils.copy(openInputStream(), out);
        }

        public long lastModified() {
            return modified;
        }

        public String getExtra() {
            return extra;
        }

    @Override
    public long size() throws Exception {
        // There are no easy way to compute the size of the resource, copy the input stream and return the length
        return IOUtils.toByteArray(resource.url().openStream()).length;
    }

    public void setExtra(String extra) {
            this.extra = extra;
        }
}
