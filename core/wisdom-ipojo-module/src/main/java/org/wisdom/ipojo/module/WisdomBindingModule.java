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

import org.apache.felix.ipojo.manipulator.spi.AbsBindingModule;
import org.apache.felix.ipojo.manipulator.spi.AnnotationVisitorFactory;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.objectweb.asm.AnnotationVisitor;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Model;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.annotations.View;

import java.lang.annotation.ElementType;

import static org.apache.felix.ipojo.manipulator.spi.helper.Predicates.on;

/**
 * Declares the bindings between Wisdom annotations and annotation visitors.
 */
public class WisdomBindingModule extends AbsBindingModule {
    /**
     * Adds the Wisdom annotation to the iPOJO manipulator.
     */
    @Override
    public void configure() {
        bind(Controller.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new WisdomControllerVisitor(context.getWorkbench(), context.getReporter());
                    }
                });

        bind(Service.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new WisdomServiceVisitor(context.getWorkbench(), context.getReporter());
                    }
                });

        bind(View.class)
                .when(on(ElementType.FIELD))
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new WisdomViewVisitor(context.getWorkbench(), context.getReporter(),
                                context.getFieldNode());
                    }
                });

        bind(Model.class)
                .when(on(ElementType.FIELD))
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new WisdomModelVisitor(context.getWorkbench(), context.getReporter(),
                                context.getFieldNode());
                    }
                });
    }
}
