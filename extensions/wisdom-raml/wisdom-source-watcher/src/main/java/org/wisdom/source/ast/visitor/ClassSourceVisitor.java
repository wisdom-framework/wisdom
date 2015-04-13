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

    public Boolean visit(ClassOrInterfaceDeclaration n, Object arg) {
        List<AnnotationExpr> annos = n.getAnnotations() == null  ? EMPTY_LIST : n.getAnnotations();

        for(AnnotationExpr anno: annos){
            if(anno.getName().equals(CONTROL_ANNO_NAME)){
                return true;
            }

        }

        List<ClassOrInterfaceType> extd = n.getExtends() == null ? EMPTY_LIST:n.getExtends();

        for(ClassOrInterfaceType ctype : extd){
            if(ctype.getName().equals(DefaultController.class.getSimpleName())){
                return true;
            }
        }

        extd = n.getImplements() == null ? EMPTY_LIST : n.getImplements();

        for(ClassOrInterfaceType ctype : extd){
            if(ctype.getName().equals(Controller.class.getSimpleName())){
                return true;
            }
        }

        return false;
    }
}
