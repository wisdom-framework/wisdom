package org.ow2.chameleon.wisdom.ipojo.module;

import org.apache.felix.ipojo.manipulator.Reporter;
import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;

/**
 * Visits the @Controller annotation and generates the @Component, @Provides and @Instantiates elements.
 */
public class WisdomControllerVisitor extends EmptyVisitor implements AnnotationVisitor {

    private final Reporter reporter;
    private final ComponentWorkbench workbench;
    private Element component = new Element("component", "");

    public WisdomControllerVisitor(ComponentWorkbench workbench, Reporter reporter) {
        this.reporter = reporter;
        this.workbench = workbench;
    }

    /**
     * End of the visit.
     * Declare the "component", "provides" and "instance" elements.
     *
     * @see org.objectweb.asm.commons.EmptyVisitor#visitEnd()
     */
    public void visitEnd() {

        String classname = workbench.getType().getClassName();

        component.addAttribute(new Attribute("classname", classname));

        // Generates the provides attribute.
        component.addElement(getProvidesElement());

        // Detect that Controller is implemented
        if (!workbench.getClassNode().interfaces.contains(Type.getInternalName(Controller.class))
                && !Type.getInternalName(DefaultController.class).equals(workbench.getClassNode().superName)) {
            reporter.warn("Cannot ensure that the class " + workbench.getType().getClassName() + " implements the " +
                    Controller.class.getName() + " interface.");
        }

        // Add the instance
        workbench.setInstance(getInstanceElement());


        if (workbench.getRoot() == null) {
            workbench.setRoot(component);
        } else {
            // Error case: 2 component type's annotations (@Component and @Handler for example) on the same class
            reporter.error("Multiple 'component type' annotations on the class '{%s}'.", classname);
            reporter.warn("@Controller will be ignored.");
        }
    }

    private Element getInstanceElement() {
        Element instance = new Element("instance", "");
        instance.addAttribute(new Attribute("component", workbench.getType().getClassName()));
        return instance;
    }

    private Element getProvidesElement() {
        Element provides = new Element("provides", "");
        //provides.addAttribute(new Attribute("specifications", Controller.class.getName()));
        return provides;
    }

}
