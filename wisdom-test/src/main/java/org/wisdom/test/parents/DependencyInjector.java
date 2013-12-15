package org.wisdom.test.parents;


import org.osgi.framework.BundleContext;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.Controller;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Injects all @Inject fields found in the given object's class.
 * Support Bundle Context injection, Template injection and Service injection.
 *
 * Be aware that this class is heavily relying on reflection and type matching as we are in a different classloader.
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
        if (field.getType().getName().equals(BundleContext.class.getName())) {
            set(object, field, helper.getContext());
        } else if (field.getType().getName().equals(Template.class.getName())) {
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
                Object template = helper.getServiceObject(Template.class.getName(), "(name=" + name + ")");
                if (template == null) {
                    throw new ExceptionInInitializerError("Cannot inject a template in " + field.getName() + ", " +
                            "cannot find a template with name=" + name);
                }
                set(object, field, template);
            }
            if (filter != null) {
                Object template = helper.waitForService(Template.class.getName(), filter, 10000, false);
                if (template == null) {
                    throw new ExceptionInInitializerError("Cannot inject a template in " + field.getName() + ", " +
                            "cannot find a template matching the given filter: " + filter);
                }
                set(object, field, template);
            }
        } else if (field.getType().getName().equals(Router.class.getName())) {
            Object router = helper.waitForService(Router.class, null, 10000, false);
            set(object, field, router);
        } else if (isController(field.getType())) {
            // Controller are identified by their classname (matching the factory.name).
            String filter = String.format("(factory.name=%s)", field.getType().getName());
            Object controller = helper.waitForService(Controller.class, filter, 10000, false);
            if (controller == null) {
                throw new ExceptionInInitializerError("Cannot inject a controller in " + field.getName() + ", " +
                        "cannot find a controller matching the given filter: " + filter);
            }
            set(object, field, controller);
        } else {
            // Service
            String filter = readFilterAnnotation(field);
            Object service = helper.waitForService(field.getType(), filter, 10000, false);
            if (service == null) {
                throw new ExceptionInInitializerError("Cannot inject a service in " + field.getName() + ", " +
                        "cannot find a service publishing " + field.getType().getName() + " matching the filter " +
                        filter);
            }
            set(object, field, service);
        }
    }

    private static boolean isController(Class<?> type) {
        List<String> classes = traverseHierarchy(type);
        return classes.contains(Controller.class.getName());
    }

    private static List<String> traverseHierarchy(Class<?> type) {
        List<String> list = new ArrayList<>();
        list.add(type.getName());
        for (Class clazz : type.getInterfaces()) {
            list.addAll(traverseHierarchy(clazz));
        }
        if (type.getSuperclass() != null) {
            list.addAll(traverseHierarchy(type.getSuperclass()));
        }
        return list;
    }

    private static String readNameAnnotation(Field field) {
        // We can't access the annotation directly because of the classloading.
        // We retrieve the value by reflection.
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType().getName().endsWith(Name.class.getName())) {
                try {
                    Method method = annotation.getClass().getMethod("value");
                    return (String) method.invoke(annotation);
                } catch (Exception e) { //NOSONAR
                    throw new ExceptionInInitializerError("Cannot retrieve the value of the @Name annotation");
                }
            }
        }
        return null;
    }

    private static String readFilterAnnotation(Field field) {
        // We can't access the annotation directly because of the classloading.
        // We retrieve the value by reflection.
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType().getName().endsWith(Filter.class.getName())) {
                try {
                    Method method = annotation.getClass().getMethod("value");
                    return (String) method.invoke(annotation);
                } catch (Exception e) { //NOSONAR
                    throw new ExceptionInInitializerError("Cannot retrieve the value of the @Filter annotation");
                }
            }
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
