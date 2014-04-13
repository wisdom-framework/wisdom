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
import org.wisdom.api.model.Crud;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check that the @Model annotation is parsed correctly.
 */
public class WisdomModelVisitorTest {

    private java.util.Map<Element, String> elements = new HashMap<>();

    @After
    public void tearDown() {
        elements.clear();
    }

    @Test
    public void parseRegularModel() {
        Reporter reporter = new SystemReporter();
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());
        when(workbench.getElements()).thenReturn(elements);

        FieldNode node = new FieldNode(Opcodes.ACC_PROTECTED, "model", Type.getDescriptor(Crud.class), null, null);

        WisdomModelVisitor visitor = new WisdomModelVisitor(workbench, reporter, node);
        visitor.visit("value", "entity");
        visitor.visitEnd();

        assertThat(elements).hasSize(1);
        Element element = elements.keySet().iterator().next();
        assertThat(element.getName()).isEqualTo("requires");
        assertThat(element.getAttribute("field")).isEqualTo("model");
        assertThat(element.getAttribute("filter")).contains("=entity)");

    }

    @Test
    public void parseEmptyAnnotation() {
        Reporter reporter = new SystemReporter();
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());
        when(workbench.getElements()).thenReturn(elements);

        FieldNode node = new FieldNode(Opcodes.ACC_PROTECTED, "model", Type.getDescriptor(Crud.class), null, null);

        WisdomModelVisitor visitor = new WisdomModelVisitor(workbench, reporter, node);
        visitor.visitEnd();

        assertThat(elements).hasSize(0);
    }


    @Test
    public void parseModelOnNotCrudField() {
        Reporter reporter = new SystemReporter();
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(MyComponent.class));
        when(workbench.getClassNode()).thenReturn(new ClassNode());
        when(workbench.getElements()).thenReturn(elements);

        FieldNode node = new FieldNode(Opcodes.ACC_PROTECTED, "model", Type.getDescriptor(String.class), null, null);

        WisdomModelVisitor visitor = new WisdomModelVisitor(workbench, reporter, node);
        visitor.visit("value", "entity");
        visitor.visitEnd();

        assertThat(elements).hasSize(1);
        Element element = elements.keySet().iterator().next();
        assertThat(element.getName()).isEqualTo("requires");
        assertThat(element.getAttribute("field")).isEqualTo("model");
        assertThat(element.getAttribute("filter")).contains("=entity)");
    }

    private class MyComponent {

    }

}
