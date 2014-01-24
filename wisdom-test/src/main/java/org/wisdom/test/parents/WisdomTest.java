package org.wisdom.test.parents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.testing.helpers.Stability;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.Status;
import org.wisdom.test.WisdomRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 */
@RunWith(WisdomRunner.class)
public class WisdomTest implements Status {

    @Inject
    public BundleContext context;
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static final String JSON_ERROR = "Cannot retrieve the json form of result `";

    public static FileItem from(File file) {
        return new FakeFileItem(file, null);
    }

    public static FileItem from(File file, String field) {
        return new FakeFileItem(file, field);
    }

    @Before
    public void ensureBundleContextInjection() throws ClassNotFoundException {
        assertThat(context).isNotNull();
        Stability.waitForStability(context);
    }

    public int status(Action.ActionResult result) {
        return result.result.getStatusCode();
    }

    public String contentType(Action.ActionResult result) {
        return result.result.getContentType();
    }

    public ObjectNode json(Action.ActionResult result) {
        try {
            return MAPPER.valueToTree(result.result.getRenderable().content());
        } catch (Exception e) {
            throw new RuntimeException(JSON_ERROR + result + "`", e);
        }
    }

    public ArrayNode jsonarray(Action.ActionResult result) {
        try {
            // Default rendering here (no extension support)
            return MAPPER.valueToTree(result.result.getRenderable().content());
        } catch (Exception e) {
            throw new RuntimeException(JSON_ERROR + result + "`", e);
        }
    }

    public String toString(Action.ActionResult result) {
        try {
            return result.result.getRenderable().content().toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the String form of result `" + result + "`", e);
        }
    }

    public byte[] toBytes(Action.ActionResult result) {
        try {
            return IOUtils.toByteArray(result.result.getRenderable().render(result.context, result.result));
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the byte[] form of result `" + result + "`", e);
        }
    }


}
