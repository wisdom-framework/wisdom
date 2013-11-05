package org.ow2.chameleon.wisdom.test.parents;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.testing.helpers.Stability;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.FileItem;
import org.ow2.chameleon.wisdom.api.http.Status;
import org.ow2.chameleon.wisdom.test.WisdomRunner;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(WisdomRunner.class)
public class  ControllerTest<T extends Controller> implements Status {

    private String controllerClass;

    @Inject
    public BundleContext context;

    public T controller;

    public ControllerTest() {
        deduceControllerType();
    }

    @Before
    public void ensureBundleContextInjection() throws ClassNotFoundException {
        assertThat(context).isNotNull();

    }

    @Before
    public void injectController() throws ClassNotFoundException {
        try {
            Stability.waitForStability(context);
            deduceControllerType();
            Collection<ServiceReference<Controller>> collection =
                    context.getServiceReferences(Controller.class, "(factory.name=" + controllerClass +")");
            if (collection.isEmpty()) {
                throw new ExceptionInInitializerError("Cannot find the controller " + controllerClass + " -" +
                        " test aborted");
            } else {
                Controller service = context.getService(collection.iterator().next());
                controller = (T) service;
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Wrong filter", e);
        }
    }

    private void deduceControllerType()  {
        Type type = this.getClass().getGenericSuperclass();
        if (type == null  || !(type instanceof ParameterizedType)) {
            throw new ExceptionInInitializerError("Cannot create the controller test, " +
                    "the controller class must be indicated as 'generic' parameter");
        }
        ParameterizedType parameterized = (ParameterizedType) type;
        controllerClass = ((Class) parameterized.getActualTypeArguments()[0]).getName();
    }

    public int status(Action.ActionResult result) {
        return result.result.getStatusCode();
    }

    public String contentType(Action.ActionResult result) {
        return result.result.getContentType();
    }

    public ObjectNode json(Action.ActionResult result) {
        try {
            String s = IOUtils.toString(result.result.getRenderable().render(result.context, result.result));
            return (new ObjectMapper()).readValue(s, ObjectNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the json form of result `" + result + "`", e);
        }
    }

    public ArrayNode jsonarray(Action.ActionResult result) {
        try {
            String s = IOUtils.toString(result.result.getRenderable().render(result.context, result.result));
            return (new ObjectMapper()).readValue(s, ArrayNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the json form of result `" + result + "`", e);
        }
    }

    public String toString(Action.ActionResult result) {
        try {
            return IOUtils.toString(result.result.getRenderable().render(result.context, result.result));
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

    public static FileItem from(File file) {
        return new FakeFileItem(file, null);
    }

    public static FileItem from(File file, String field) {
        return new FakeFileItem(file, field);
    }

}
