package org.ow2.chameleon.wisdom.ipojo.module;

import org.apache.felix.ipojo.manipulator.spi.AbsBindingModule;
import org.apache.felix.ipojo.manipulator.spi.AnnotationVisitorFactory;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.objectweb.asm.AnnotationVisitor;
import org.ow2.chameleon.wisdom.api.annotations.Controller;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 20/11/2013
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
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
    }
}
