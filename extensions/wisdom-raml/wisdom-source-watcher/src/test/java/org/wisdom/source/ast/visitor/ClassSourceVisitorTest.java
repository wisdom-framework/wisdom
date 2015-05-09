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


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * check the {@link ClassSourceVisitor}
 */
public class ClassSourceVisitorTest {

    ClassSourceVisitor visitor = new ClassSourceVisitor();

    @Test
    public void testClassImplementingController() throws IOException, ParseException {
        File file = new File("src/test/java/sample/MyController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        final Boolean isController = visitor.visit(declaration, null);
        assertThat(isController).isTrue();
    }

    @Test
    public void testClassExtendingDefaultController() throws IOException, ParseException {
        File file = new File("src/test/java/sample/MyDefaultController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        final Boolean isController = visitor.visit(declaration, null);
        assertThat(isController).isTrue();
    }

    @Test
    public void testClassAnnotatedWithController() throws IOException, ParseException {
        File file = new File("src/test/java/sample/MyAnnotatedController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        final Boolean isController = visitor.visit(declaration, null);
        assertThat(isController).isTrue();
    }

    @Test
    public void testABasicClass() throws IOException, ParseException {
        File file = new File("src/test/java/sample/MyClass.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        final Boolean isController = visitor.visit(declaration, null);
        assertThat(isController).isFalse();
    }

    @Test
    public void testAnInterfaceClass() throws IOException, ParseException {
        File file = new File("src/test/java/sample/MyInterface.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        final Boolean isController = visitor.visit(declaration, null);
        assertThat(isController).isFalse();
    }

    @Test
    public void testAnEnum() throws IOException, ParseException {
        File file = new File("src/test/java/sample/MyEnum.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        final Boolean isController = visitor.visit(declaration, null);
        assertThat(isController).isFalse();
    }

    @Test
    public void testAnAnnotation() throws IOException, ParseException {
        File file = new File("src/test/java/sample/MyAnnotation.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        final Boolean isController = visitor.visit(declaration, null);
        assertThat(isController).isFalse();
    }

    @Test(expected = ParseException.class)
    public void testANonJavaClass() throws IOException, ParseException {
        File file = new File("src/test/java/sample/readme.txt");
        JavaParser.parse(file);
    }

    @Test(expected = IOException.class)
    public void testANotExistingClass() throws IOException, ParseException {
        File file = new File("src/test/java/sample/missing.java");
        JavaParser.parse(file);
    }


}