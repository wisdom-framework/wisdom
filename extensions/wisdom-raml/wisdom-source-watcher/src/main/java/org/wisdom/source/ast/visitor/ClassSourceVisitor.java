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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;

import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * Visit @{ClassOrInterfaceDeclaration} and to know if the visited class is a Wisdom Controller.
 *
 * @author barjo
 */
public class ClassSourceVisitor extends GenericVisitorAdapter<Boolean,Object>{

    private static final String CONTROL_ANNO_NAME = org.wisdom.api.annotations.Controller.class.getSimpleName();

    /**
     * Visit the Class declaration and return true if it corresponds to a wisdom controller.
     *
     * @param declaration The class declaration created by the JavaParser.
     * @param extra Extra out value argument, not used here.
     * @return <code>true</code> if the declaration correspond to a wisdom controller, <code>false</code> otherwise.
     */
    public Boolean visit(ClassOrInterfaceDeclaration declaration, Object extra) {

        //noinspection unchecked
        List<AnnotationExpr> annos = declaration.getAnnotations() == null  ? EMPTY_LIST : declaration.getAnnotations();

        for(AnnotationExpr anno: annos){
            if(anno.getName().getName().equals(CONTROL_ANNO_NAME)){
                return true;
            }

        }

        //noinspection unchecked
        List<ClassOrInterfaceType> extds = declaration.getExtends() == null ? EMPTY_LIST:declaration.getExtends();

        for(ClassOrInterfaceType ctype : extds){
            if(ctype.getName().equals(DefaultController.class.getSimpleName())){
                return true;
            }
        }

        //noinspection unchecked
        extds = declaration.getImplements() == null ? EMPTY_LIST : declaration.getImplements();

        for(ClassOrInterfaceType ctype : extds){
            if(ctype.getName().equals(Controller.class.getSimpleName())){
                return true;
            }
        }

        return false;
    }
}
