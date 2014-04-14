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
import org.apache.felix.ipojo.manipulator.reporter.SystemReporter;
import org.apache.felix.ipojo.metadata.Element;
import org.junit.After;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.wisdom.api.templates.Template;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check that the @View annotation is parsed correctly.
 */
public class WisdomViewVisitorTest {

    private java.util.Map<Element, String> elements = new HashMap<>();

    @After
    public void tearDown() {
        elements.clear();
    }

    @Test
    public void parseRegularView() {
        Reporter reporter = new SystemReporter();
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());
        when(workbench.getElements()).thenReturn(elements);

        FieldNode node = new FieldNode(Opcodes.ACC_PROTECTED, "template", Type.getDescriptor(Template.class), null, null);

        WisdomViewVisitor visitor = new WisdomViewVisitor(workbench, reporter, node);
        visitor.visit("value", "index");
        visitor.visitEnd();

        assertThat(elements).hasSize(1);
        Element element = elements.keySet().iterator().next();
        assertThat(element.getName()).isEqualTo("requires");
        assertThat(element.getAttribute("field")).isEqualTo("template");
        assertThat(element.getAttribute("filter")).contains("(name=index)");

    }

    @Test
    public void parseEmptyAnnotation() {
        Reporter reporter = new SystemReporter();
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());
        when(workbench.getElements()).thenReturn(elements);

        FieldNode node = new FieldNode(Opcodes.ACC_PROTECTED, "template",
                Type.getDescriptor(Template.class), null, null);

        WisdomViewVisitor visitor = new WisdomViewVisitor(workbench, reporter, node);
        visitor.visitEnd();

        assertThat(elements).hasSize(0);
    }


    @Test
    public void parseViewOnNotTemplateField() {
        Reporter reporter = new SystemReporter();
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());
        when(workbench.getElements()).thenReturn(elements);

        FieldNode node = new FieldNode(Opcodes.ACC_PROTECTED, "template", Type.getDescriptor(String.class), null, null);

        WisdomViewVisitor visitor = new WisdomViewVisitor(workbench, reporter, node);
        visitor.visit("value", "index");
        visitor.visitEnd();

        assertThat(elements).hasSize(1);
        Element element = elements.keySet().iterator().next();
        assertThat(element.getName()).isEqualTo("requires");
        assertThat(element.getAttribute("field")).isEqualTo("template");
        assertThat(element.getAttribute("filter")).contains("=index)");
    }

    private class MyComponent {

    }

}
