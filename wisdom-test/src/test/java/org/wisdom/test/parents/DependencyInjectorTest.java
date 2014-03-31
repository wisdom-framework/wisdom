package org.wisdom.test.parents;

import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.Controller;
import org.wisdom.api.templates.Template;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks that the dependency injector injects the right set of objects.
 */
public class DependencyInjectorTest {


    @Test
    public void testService() {
        MyInjectedClassWithServices object = new MyInjectedClassWithServices();
        OSGiHelper helper = mock(OSGiHelper.class);
        when(helper.waitForService(anyString(), anyString(), anyInt(), anyBoolean())).thenReturn(mock
                (ServiceReference.class));
        when(helper.getServiceObject(any(ServiceReference.class))).thenReturn(new Object());
        DependencyInjector.inject(object, helper);

        assertThat(object.svc).isNotNull();
        assertThat(object.filtered).isNotNull();
    }

    @Test
    public void testTemplate() {
        MyInjectedClassWithTemplates object = new MyInjectedClassWithTemplates();
        OSGiHelper helper = mock(OSGiHelper.class);
        when(helper.waitForService(anyString(), anyString(), anyInt(), anyBoolean())).thenReturn(mock
                (ServiceReference.class));
        when(helper.getServiceObject(any(ServiceReference.class))).thenReturn(mock(Template.class));
        DependencyInjector.inject(object, helper);

        assertThat(object.template).isNotNull();
        assertThat(object.filtered).isNotNull();
    }

    @Test
    public void testController() {
        MyInjectedClassWithControllers object = new MyInjectedClassWithControllers();
        OSGiHelper helper = mock(OSGiHelper.class);
        when(helper.waitForService(anyString(), anyString(), anyInt(), anyBoolean())).thenReturn(mock
                (ServiceReference.class));
        when(helper.getServiceObject(any(ServiceReference.class))).thenReturn(mock(Controller.class));
        DependencyInjector.inject(object, helper);

        assertThat(object.controller).isNotNull();
    }


    private static class MyInjectedClassWithServices {

        @Inject
        Object svc;

        @Inject
        @Filter("(name=name)")
        private Object filtered;

    }

    private static class MyInjectedClassWithTemplates {

        @Inject
        @Name("foo")
        Template template;

        @Inject
        @Filter("(name=name)")
        Template filtered;

    }

    private static class MyInjectedClassWithControllers {

        @Inject
        Controller controller;

    }

}
