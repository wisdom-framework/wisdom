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
package org.wisdom.ipojo.module;

import org.apache.felix.ipojo.manipulator.Reporter;
import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.metadata.Element;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Check that the @Service annotation generates the corresponding Element-Attribute structure.
 */
public class WisdomServiceVisitorTest {

    Element root;
    Element instance;

    @Test
    public void testOnlyService() {
        Reporter reporter = mock(Reporter.class);
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                root = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setRoot(any(Element.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                instance = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setInstance(any(Element.class));

        WisdomServiceVisitor visitor = new WisdomServiceVisitor(workbench, reporter);
        visitor.visitEnd();

        // Check the generated Component
        assertThat(root).isNotNull();
        assertThat(root.getName()).isEqualTo("component");
        assertThat(root.getAttribute("classname")).isEqualTo(MyComponent.class.getName());

        // Check the Provides element
        assertThat(root.getElements("provides")).hasSize(1);

        // Check the Instance declaration
        assertThat(instance).isNotNull();
        assertThat(instance.getAttribute("component")).isEqualTo(MyComponent.class.getName());

    }

    @Test
    public void testServiceWithOneSpecification() {
        Reporter reporter = mock(Reporter.class);
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                root = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setRoot(any(Element.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                instance = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setInstance(any(Element.class));

        WisdomServiceVisitor visitor = new WisdomServiceVisitor(workbench, reporter);
        AnnotationVisitor ann = visitor.visitArray("value");
        ann.visit(null, Type.getType(Runnable.class));
        ann.visitEnd();
        visitor.visitEnd();

        // Check the generated Component
        assertThat(root).isNotNull();
        assertThat(root.getName()).isEqualTo("component");
        assertThat(root.getAttribute("classname")).isEqualTo(MyComponent.class.getName());

        // Check the Provides element
        assertThat(root.getElements("provides")).hasSize(1);
        assertThat(root.getElements("provides")[0].getAttribute("specifications")).isNotNull().isEqualTo("{" +
                Runnable.class.getName() + "}");

        // Check the Instance declaration
        assertThat(instance).isNotNull();
        assertThat(instance.getAttribute("component")).isEqualTo(MyComponent.class.getName());

    }

    @Test
    public void testServiceWithTwoSpecifications() {
        Reporter reporter = mock(Reporter.class);
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                root = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setRoot(any(Element.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                instance = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setInstance(any(Element.class));

        WisdomServiceVisitor visitor = new WisdomServiceVisitor(workbench, reporter);
        AnnotationVisitor ann = visitor.visitArray("value");
        ann.visit(null, Type.getType(Runnable.class));
        ann.visit(null, Type.getType(List.class));
        ann.visitEnd();
        visitor.visitEnd();

        // Check the generated Component
        assertThat(root).isNotNull();
        assertThat(root.getName()).isEqualTo("component");
        assertThat(root.getAttribute("classname")).isEqualTo(MyComponent.class.getName());

        // Check the Provides element
        assertThat(root.getElements("provides")).hasSize(1);
        assertThat(root.getElements("provides")[0].getAttribute("specifications")).isNotNull().isEqualTo("{" +
                Runnable.class.getName() + "," + List.class.getName() + "}");

        // Check the Instance declaration
        assertThat(instance).isNotNull();
        assertThat(instance.getAttribute("component")).isEqualTo(MyComponent.class.getName());

    }

    @Test
    public void testWhenAnotherRootIsAlreadyDeclared() {
        Reporter reporter = mock(Reporter.class);
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        // Set another root
        when(workbench.getRoot()).thenReturn(new Element("another", "root"));
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                root = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setRoot(any(Element.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                instance = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setInstance(any(Element.class));

        WisdomServiceVisitor visitor = new WisdomServiceVisitor(workbench, reporter);
        visitor.visitEnd();

        // Check that nothing is generated
        assertThat(root).isNull();
        assertThat(instance).isNull();

    }


    private class MyComponent {

    }
}
