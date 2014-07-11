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
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;

/**
 * Visits the @Controller annotation and generates the @Component, @Provides and @Instantiates elements.
 */
public class WisdomControllerVisitor extends AnnotationVisitor {


    private final Reporter reporter;
    private final ComponentWorkbench workbench;
    private Element component = ElementHelper.getComponentElement();

    /**
     * Creates the visitor.
     * @param workbench the workbench.
     * @param reporter the reporter.
     */
    public WisdomControllerVisitor(ComponentWorkbench workbench, Reporter reporter) {
        super(Opcodes.ASM5);
        this.reporter = reporter;
        this.workbench = workbench;
    }

    /**
     * End of the visit.
     * Declare the "component", "provides" and "instance" elements.
     *
     * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
     */
    public void visitEnd() {

        String classname = workbench.getType().getClassName();

        component.addAttribute(new Attribute("classname", classname));

        // Generates the provides attribute.
        component.addElement(ElementHelper.getProvidesElement(null));

        // Detect that Controller is implemented
        if (!workbench.getClassNode().interfaces.contains(Type.getInternalName(Controller.class))
                && !Type.getInternalName(DefaultController.class).equals(workbench.getClassNode().superName)) {
            reporter.warn("Cannot ensure that the class " + workbench.getType().getClassName() + " implements the " +
                    Controller.class.getName() + " interface.");
        }

        if (workbench.getRoot() == null) {
            workbench.setRoot(component);
            // Add the instance
            workbench.setInstance(ElementHelper.declareInstance(workbench));
        } else {
            // Error case: 2 component type's annotations (@Component and @Handler for example) on the same class
            reporter.error("Multiple 'component type' annotations on the class '{%s}'.", classname);
            reporter.warn("@Controller is ignored.");
        }
    }

}
