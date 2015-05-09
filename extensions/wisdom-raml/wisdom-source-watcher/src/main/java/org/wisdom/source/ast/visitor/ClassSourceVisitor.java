/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.source.ast.visitor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Visit @{ClassOrInterfaceDeclaration} and to know if the visited class is a Wisdom Controller.
 * <p>
 * <p>
 * A class is considered a controller if at least one of this conditions is meet:
 * - it is annotated by the {@link #CONTROL_ANNO_NAME} annotation,
 * - it extends the wisdom {@link DefaultController},
 * - it implements the wisdom {@link Controller} interface.
 * </p>
 *
 * @author barjo
 */
public class ClassSourceVisitor extends GenericVisitorAdapter<Boolean, Object> {

    /**
     * The simple name of the Controller annotation.
     */
    private static final String CONTROL_ANNO_NAME = org.wisdom.api.annotations.Controller.class.getSimpleName();

    /**
     * The simple name of the Controller and DefaultController class.
     */
    private static final Set<String> CONTROL_CLASSNAMES = new HashSet<>(2);

    static {
        CONTROL_CLASSNAMES.add(Controller.class.getSimpleName());
        CONTROL_CLASSNAMES.add(DefaultController.class.getSimpleName());
    }

    /**
     * We need to override this method to manage the case where the visitor returns {@code null}. {@code null} is
     * considered as {@code false}.
     *
     * @param n   the unit
     * @param arg meaningless
     * @return whether or not the compilation unit is a controller.
     */
    public Boolean visit(final CompilationUnit n, final Object arg) {
        final Boolean result = super.visit(n, arg);
        return result != null && result;
    }

    /**
     * Visit the Class declaration and return true if it corresponds to a wisdom controller.
     *
     * @param declaration The class declaration created by the JavaParser.
     * @param extra       Extra out value argument, not used here.
     * @return <code>true</code> if the declaration correspond to a wisdom controller, <code>false</code> otherwise.
     */
    public Boolean visit(ClassOrInterfaceDeclaration declaration, Object extra) {

        if (declaration.getAnnotations() != null
                && containsAnnotation(declaration.getAnnotations(), CONTROL_ANNO_NAME)) {
            return true;
        }

        //Get the list of extended and implemented class
        List<ClassOrInterfaceType> hierarchy = new ArrayList<>();

        if (declaration.getExtends() != null) {
            hierarchy.addAll(declaration.getExtends());
        }

        if (declaration.getImplements() != null) {
            hierarchy.addAll(declaration.getImplements());
        }

        return containsClassName(hierarchy, CONTROL_CLASSNAMES);
    }

    /**
     * Check if the list of annotation contains the annotation  of given name.
     *
     * @param annos,          the annotation list
     * @param annotationName, the annotation name
     * @return <code>true</code> if the annotation list contains the given annotation.
     */
    private boolean containsAnnotation(List<AnnotationExpr> annos, String annotationName) {
        for (AnnotationExpr anno : annos) {
            if (anno.getName().getName().equals(annotationName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the list of class or interface contains a class which name is given in the <code>simpleNames</code> set.
     *
     * @param klassList   The list of class or interface
     * @param simpleNames a set of class simple name.
     * @return <code>true</code> if the list contains a class or interface which name is present in the
     * <code>simpleNames</code> set.
     */
    private boolean containsClassName(List<ClassOrInterfaceType> klassList, Set<String> simpleNames) {
        for (ClassOrInterfaceType ctype : klassList) {
            if (simpleNames.contains(ctype.getName())) {
                return true;
            }
        }
        return false;
    }
}
