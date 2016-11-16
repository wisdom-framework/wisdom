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
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.wisdom.api.http.FileItem;
import org.wisdom.source.ast.model.ControllerModel;
import org.wisdom.source.ast.model.ControllerRouteModel;
import org.wisdom.source.ast.model.RouteParamModel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the model computed by {@link ControllerSourceVisitor}
 */
public class ControllerSourceVisitorTest {

    ControllerSourceVisitor visitor = new ControllerSourceVisitor();


    @Test
    public void testHttpVerbs() throws IOException, ParseException {
        File file = new File("src/test/java/controller/SimpleController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        final Collection<ControllerRouteModel> routes = (Collection<ControllerRouteModel>) model.getRoutes().get("/simple");
        assertThat(routes).isNotNull();
        assertThat(routes).hasSize(6);

        for (ControllerRouteModel route : routes) {
            assertThat(route.getHttpMethod().name()).isEqualToIgnoringCase(route.getMethodName());
            assertThat(route.getParams()).isEmpty();
            assertThat(route.getPath()).isEqualToIgnoringCase("/simple");
        }
    }

    @Test
    public void testEmptyController() throws IOException, ParseException {
        File file = new File("src/test/java/controller/EmptyController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);
        assertThat(model.getRoutes()).isEmpty();
    }

    @Test
    public void testParameters() throws IOException, ParseException {
        File file = new File("src/test/java/controller/ParameterizedController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        ControllerRouteModel route = getModelByPath(model, "/params/1");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        RouteParamModel param = (RouteParamModel) Iterables.get(route.getParams(), 0);
        assertThat(param.getName()).isEqualTo("hello");
        assertThat(param.getParamType()).isEqualTo(RouteParamModel.ParamType.FORM);
        assertThat(param.getDefaultValue()).isNull();
        assertThat(param.getValueType()).isEqualTo(String.class.getSimpleName());


        route = getModelByPath(model, "/params/2/{hello}");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        param = (RouteParamModel) Iterables.get(route.getParams(), 0);
        assertThat(param.getName()).isEqualTo("hello");
        assertThat(param.getParamType()).isEqualTo(RouteParamModel.ParamType.PATH_PARAM);
        assertThat(param.getDefaultValue()).isNull();
        assertThat(param.getValueType()).isEqualTo(String.class.getSimpleName());

        route = getModelByPath(model, "/params/3/{hello}");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(2);
        param = getParamByName(route.getParams(), "hello");
        assertThat(param.getParamType()).isEqualTo(RouteParamModel.ParamType.PATH_PARAM);
        assertThat(param.getDefaultValue()).isNull();
        assertThat(param.getValueType()).isEqualTo(String.class.getSimpleName());

        param = getParamByName(route.getParams(), "name");
        assertThat(param.getParamType()).isEqualTo(RouteParamModel.ParamType.FORM);
        assertThat(param.getDefaultValue()).isEqualTo("wisdom");
        assertThat(param.getValueType()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void testNotNullContraints() throws IOException, ParseException{
        File file = new File("src/test/java/controller/ControllerWithConstraints.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        ControllerRouteModel route = getModelByPath(model,"/superman");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        RouteParamModel param = (RouteParamModel) Iterables.get(route.getParams(), 0);

        //Annotated with NotNull constraints
        assertThat(param.getName()).isEqualTo("clark");
        assertThat(param.isMandatory()).isTrue();

        route = getModelByPath(model,"/batman");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        param = (RouteParamModel) Iterables.get(route.getParams(), 0);

        //Annotated with NotNull constraints that contains a message
        assertThat(param.getName()).isEqualTo("bruce");
        assertThat(param.isMandatory()).isTrue();
    }

    @Test
    public void testMinContraints() throws IOException, ParseException{
        File file = new File("src/test/java/controller/ControllerWithConstraints.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        ControllerRouteModel route = getModelByPath(model,"/spiderman");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        RouteParamModel param = (RouteParamModel) Iterables.get(route.getParams(), 0);

        //Annotated with Min constraint
        assertThat(param.getName()).isEqualTo("peter");
        assertThat(param.getMin()).isEqualTo(1962);

        route = getModelByPath(model,"/chameleon");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        param = (RouteParamModel) Iterables.get(route.getParams(), 0);

        //Annotated with Min constraints that contains a message
        assertThat(param.getName()).isEqualTo("dmitri");
        assertThat(param.getMin()).isEqualTo(1963);
    }

    @Test
    public void testMaxContraints() throws IOException, ParseException{
        File file = new File("src/test/java/controller/ControllerWithConstraints.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        ControllerRouteModel route = getModelByPath(model,"/rahan");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        RouteParamModel param = (RouteParamModel) Iterables.get(route.getParams(), 0);

        //Annotated with Max constraint
        assertThat(param.getName()).isEqualTo("son");
        assertThat(param.getMax()).isEqualTo(2010);

        route = getModelByPath(model,"/crao");
        assertThat(route).isNotNull();
        assertThat(route.getParams()).hasSize(1);
        param = (RouteParamModel) Iterables.get(route.getParams(), 0);

        //Annotated with Max constraints that contains a message
        assertThat(param.getName()).isEqualTo("father");
        assertThat(param.getMax()).isEqualTo(2010);
    }

    @Test
    public void testBody() throws IOException, ParseException {
        File file = new File("src/test/java/controller/ParameterizedController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        ControllerRouteModel route = getModelByPath(model, "/params/4");
        assertThat(route.getParams()).hasSize(1);
        final RouteParamModel o = (RouteParamModel) Iterables.get(route.getParams(), 0);
        assertThat(o.getParamType()).isEqualTo(RouteParamModel.ParamType.BODY);
        assertThat(o.getName()).isEqualTo("body"); // Special value.
        assertThat(o.getValueType()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void testFileItem() throws IOException, ParseException {
        File file = new File("src/test/java/controller/ParameterizedController.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        ControllerRouteModel route = getModelByPath(model, "/params/5");
        assertThat(route.getParams()).hasSize(1);
        final RouteParamModel o = (RouteParamModel) Iterables.get(route.getParams(), 0);
        assertThat(o.getParamType()).isEqualTo(RouteParamModel.ParamType.FORM);
        assertThat(o.getName()).isEqualTo("file");
        assertThat(o.getValueType()).isEqualTo(FileItem.class.getSimpleName());
    }

    @Test
    public void testControllerUsingPath() throws IOException, ParseException {
        File file = new File("src/test/java/controller/ControllerWithPath.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        System.out.println(model.getRoutesAsMultiMap());
        ControllerRouteModel route = getModelByFullPath(model, "/root/simple");
        assertThat(route).isNotNull();

        route = getModelByFullPath(model, "/root/");
        assertThat(route).isNotNull();

        route = getModelByFullPath(model, "/root");
        assertThat(route).isNotNull();

        route = getModelByFullPath(model, "/rootstuff");
        assertThat(route).isNotNull();

    }

    @Test
    public void testReferencingConstant() throws IOException, ParseException {
        File file = new File("src/test/java/controller/ControllerReferencingConstant.java");
        final CompilationUnit declaration = JavaParser.parse(file);
        ControllerModel model = new ControllerModel();
        visitor.visit(declaration, model);

        final Collection<ControllerRouteModel> routes = (Collection<ControllerRouteModel>) model.getRoutes().get("/constant");
        assertThat(routes).isNotNull();
        assertThat(routes).hasSize(3);

        for (ControllerRouteModel route : routes) {
            assertThat(route.getHttpMethod().name()).isEqualToIgnoringCase(route.getMethodName());
            assertThat(route.getParams()).isEmpty();
            assertThat(route.getPath()).isEqualToIgnoringCase("/constant");
        }
    }

    private ControllerRouteModel getModelByPath(ControllerModel model, String name) {
        for (ControllerRouteModel route : (Collection<ControllerRouteModel>) model.getRoutesAsMultiMap().values()) {
            if (route.getPath().equals(name)) {
                return route;
            }
        }
        return null;
    }

    private ControllerRouteModel getModelByFullPath(ControllerModel model, String name) {
        final Collection<ControllerRouteModel> collection = model.getRoutesAsMultiMap().get(name);
        if (collection.isEmpty()) {
            return null;
        }
        return Iterables.get(collection, 0);
    }

    private RouteParamModel getParamByName(Collection<RouteParamModel> set, String name) {
        for (RouteParamModel model : set) {
            if (model.getName().equalsIgnoreCase(name)) {
                return model;
            }
        }
        return null;
    }

    // PArameter
    // Body
    // Path
    // No uri
    // no / in uri
    // regex in path

}