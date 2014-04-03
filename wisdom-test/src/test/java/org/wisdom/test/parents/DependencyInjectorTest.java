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
