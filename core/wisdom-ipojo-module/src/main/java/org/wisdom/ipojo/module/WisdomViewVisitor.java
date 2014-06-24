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
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.wisdom.api.templates.Template;

/**
 * Visits the @View annotation and transform it into a @Requires with the right filter.
 */
public class WisdomViewVisitor extends AnnotationVisitor {
    private final Reporter reporter;
    private final ComponentWorkbench workbench;
    private final String field;
    private final FieldNode node;
    private String name;

    /**
     * Creates the visitor.
     * @param workbench the workbench
     * @param reporter the reporter
     * @param node the field node.
     */
    public WisdomViewVisitor(ComponentWorkbench workbench, Reporter reporter, FieldNode node) {
        super(Opcodes.ASM5);
        this.reporter = reporter;
        this.workbench = workbench;
        this.node = node;
        this.field = node.name;
    }

    /**
     * Visits the @Template annotation value.
     * @param s value
     * @param o the value
     */
    @Override
    public void visit(String s, Object o) {
        this.name = o.toString();
    }

    /**
     * Generates the element-attribute structure.
     */
    @Override
    public void visitEnd() {
        if (name == null  || name.length() == 0) {
            reporter.error("The 'name' attribute of @View from " + workbench.getType().getClassName() + " must be " +
                    "set");
            return;
        }

        // Check the type of the field
        if (! Type.getDescriptor(Template.class).equals(node.desc)) {
            reporter.error("The type of the field " + field + " from " + workbench.getType().getClassName() + " must " +
                    "be " + Template.class.getName() + " because the field is annotated with @View");
        }

        Element requires = new Element("requires", "");
        requires.addAttribute(new Attribute("field", field));
        requires.addAttribute(new Attribute("filter", getFilter(name)));

        workbench.getElements().put(requires, null);
    }

    private String getFilter(String name) {
        return "(name=" + name + ")";
    }
}
