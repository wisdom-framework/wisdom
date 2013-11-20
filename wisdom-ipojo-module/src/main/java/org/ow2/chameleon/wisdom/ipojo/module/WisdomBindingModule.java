package org.ow2.chameleon.wisdom.ipojo.module;

import org.apache.felix.ipojo.manipulator.spi.AbsBindingModule;
import org.apache.felix.ipojo.manipulator.spi.AnnotationVisitorFactory;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.tree.FieldNode;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.View;

import java.lang.annotation.ElementType;

import static org.apache.felix.ipojo.manipulator.spi.helper.Predicates.on;

/**
 * Declares the bindings between Wisdom annotations and annotation visitors.
 */
public class WisdomBindingModule extends AbsBindingModule {
    @Override
    public void configure() {
        bind(Controller.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new WisdomControllerVisitor(context.getWorkbench(), context.getReporter());
                    }
                });

        bind(View.class)
                .when(on(ElementType.FIELD))
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new WisdomViewVisitor(context.getWorkbench(), context.getReporter(),
                                ((FieldNode) context.getNode()));
                    }
                });
    }
}
