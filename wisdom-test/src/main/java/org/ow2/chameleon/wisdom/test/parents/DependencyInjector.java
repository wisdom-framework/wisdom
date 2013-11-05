package org.ow2.chameleon.wisdom.test.parents;


import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.ow2.chameleon.testing.helpers.Stability;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.Status;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;
import org.ow2.chameleon.wisdom.test.WisdomRunner;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Injects all @Inject fields found in the given object's class.
 * Support Bundle Context injection, Template injection and Service injection.
 */
public class DependencyInjector {


    public static void inject(Object object, BundleContext context, OSGiHelper helper) {
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            if (field.getAnnotation(javax.inject.Inject.class) != null) {
                inject(object, field, helper);
            }
        }
        fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(javax.inject.Inject.class) != null) {
                inject(object, field, helper);
            }
        }
    }


    public static void inject(Object object, Field field, OSGiHelper helper) {
        if (field.getType().equals(BundleContext.class)) {
            set(object, field, helper.getContext());
        } else if (field.getType().equals(Template.class)) {
            String name = readNameAnnotation(field);
            String filter = readFilterAnnotation(field);
            if (name == null  && filter == null) {
                throw new ExceptionInInitializerError("Cannot inject a template in " + field.getName() + ", " +
                        "the @Name annotation or @Filter annotation are required to indicate the template to inject");
            }
            if (name != null  && filter != null) {
                throw new ExceptionInInitializerError("Cannot inject a template in " + field.getName() + ", " +
                        "both @Name annotation and @Filter annotations are used to indicate the template to " +
                        "inject, please use only one");
            }
            if (name != null) {
                Template template = helper.getServiceObject(Template.class, "(name=" + name + ")");
                if (template == null) {
                    throw new ExceptionInInitializerError("Cannot inject a template in " + field.getName() + ", " +
                            "cannot find a template with name=" + name);
                }
                set(object, field, template);
            }
            if (filter != null) {
                Template template = helper.waitForService(Template.class, filter, 10000);
                if (template == null) {
                    throw new ExceptionInInitializerError("Cannot inject a template in " + field.getName() + ", " +
                            "cannot find a template matching the given filter: " + filter);
                }
                set(object, field, template);
            }
        } else if (field.getType().equals(Router.class)) {
            set(object, field, helper.waitForService(Router.class, null, 10000));
        } else if (Controller.class.isAssignableFrom(Controller.class)) {
            // Controller are identified by their classname (matching the factory.name).
            String filter = String.format("(factory.name=%s)", field.getType().getName());
            Object controller = helper.waitForService(Controller.class, filter, 10000);
            if (controller == null) {
                throw new ExceptionInInitializerError("Cannot inject a controller in " + field.getName() + ", " +
                        "cannot find a controller matching the given filter: " + filter);
            }
            set(object, field, controller);
        } else {
            // Service
            String filter = readFilterAnnotation(field);
            Object service = helper.waitForService(field.getType(), filter, 10000);
            if (service == null) {
                throw new ExceptionInInitializerError("Cannot inject a service in " + field.getName() + ", " +
                        "cannot find a service publishing " + field.getType().getName() + " matching the filter " +
                        filter);
            }
            set(object, field, service);
        }
    }

    private static String readNameAnnotation(Field field) {
        Name name = field.getAnnotation(Name.class);
        if (name != null) {
            return name.value();
        }
        return null;
    }

    private static String readFilterAnnotation(Field field) {
        Filter name = field.getAnnotation(Filter.class);
        if (name != null) {
            return name.value();
        }
        return null;
    }

    private static void set(Object object, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error when injecting " + value + " in " + field, e);
        }
    }


}
